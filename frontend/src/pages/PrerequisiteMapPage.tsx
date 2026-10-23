import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getPrograms } from '../api/auth';
import { getPrerequisiteMap } from '../api/prerequisites';
import { getMyCourses } from '../api/studentCourses';
import { useAuth } from '../auth/AuthContext';
import {
  ancestorsOf,
  buildGraph,
  descendantsOf,
  requirementGroups,
} from '../utils/prereqGraph';

const NODE_W = 150;
const NODE_H = 50;
const COL_GAP = 110;
const ROW_GAP = 14;
const PAD = 16;

function shorten(text: string, max: number): string {
  return text.length > max ? `${text.slice(0, max - 1)}…` : text;
}

export default function PrerequisiteMapPage() {
  const { user } = useAuth();
  const programId = user?.programId ?? null;
  const [selected, setSelected] = useState<string | null>(null);

  const map = useQuery({
    queryKey: ['prereqMap', programId],
    queryFn: () => getPrerequisiteMap(programId as number),
    enabled: programId !== null,
  });
  const programs = useQuery({ queryKey: ['programs'], queryFn: getPrograms });
  const mine = useQuery({ queryKey: ['myCourses'], queryFn: getMyCourses });

  const graph = useMemo(() => buildGraph(map.data ?? []), [map.data]);

  const status = useMemo(() => {
    const result = new Map<string, string>();
    for (const c of mine.data ?? []) {
      result.set(c.code, c.status);
    }
    return result;
  }, [mine.data]);

  const positions = useMemo(() => {
    const result = new Map<string, { x: number; y: number }>();
    for (const n of graph.nodes) {
      result.set(n.course.code, {
        x: PAD + n.layer * (NODE_W + COL_GAP),
        y: PAD + n.row * (NODE_H + ROW_GAP),
      });
    }
    return result;
  }, [graph.nodes]);

  const ancestors = useMemo(
    () => (selected ? ancestorsOf(selected, graph.edges) : new Set<string>()),
    [selected, graph.edges],
  );
  const descendants = useMemo(
    () => (selected ? descendantsOf(selected, graph.edges) : new Set<string>()),
    [selected, graph.edges],
  );

  if (programId === null) {
    return <p>Choose a major on the Profile page to see its prerequisite map.</p>;
  }
  if (map.isLoading) return <p>Loading...</p>;
  if (map.isError || !map.data) return <p className="warn">Could not load the prerequisite map.</p>;

  const programName = programs.data?.find((p) => p.id === programId);
  const width = PAD * 2 + graph.layerCount * NODE_W + Math.max(graph.layerCount - 1, 0) * COL_GAP;
  const height = PAD * 2 + graph.rowCount * NODE_H + Math.max(graph.rowCount - 1, 0) * ROW_GAP;

  const nodeActive = (code: string) =>
    selected === null || code === selected || ancestors.has(code) || descendants.has(code);
  const edgeActive = (e: { from: string; to: string }) =>
    selected === null ||
    ((e.to === selected || ancestors.has(e.to)) && ancestors.has(e.from)) ||
    ((e.from === selected || descendants.has(e.from)) && descendants.has(e.to));

  const selectedCourse = graph.nodes.find((n) => n.course.code === selected)?.course;
  const needs = selectedCourse ? requirementGroups(selectedCourse.prereqTree) : [];
  const unlocks = selected
    ? [...new Set(graph.edges.filter((e) => e.from === selected).map((e) => e.to))]
    : [];

  return (
    <>
      <h1>Prerequisite Map</h1>
      <p>
        {programName
          ? `${programName.degreeType} ${programName.name} (${programName.catalogYear})`
          : 'Your degree'}
        . Arrows point from a prerequisite to the course that needs it. Click a course to
        highlight what you need first and what it unlocks.
      </p>

      <div className="pm-legend">
        <span><i className="swatch" /> Not taken</span>
        <span><i className="swatch planned" /> Planned or in progress</span>
        <span><i className="swatch done" /> Completed</span>
        <span>Solid line: required</span>
        <span>Dashed line: one of several</span>
      </div>

      {graph.nodes.length === 0 ? (
        <p>No prerequisite relationships are entered for this major yet.</p>
      ) : (
        <div className="pm-wrap">
          <svg width={width} height={height} role="img" aria-label="Prerequisite map">
            <defs>
              <marker
                id="pm-arrow"
                viewBox="0 0 10 10"
                refX="9"
                refY="5"
                markerWidth="7"
                markerHeight="7"
                orient="auto-start-reverse"
              >
                <path d="M 0 0 L 10 5 L 0 10 z" fill="#9aa5b1" />
              </marker>
            </defs>

            {graph.edges.map((e) => {
              const from = positions.get(e.from);
              const to = positions.get(e.to);
              if (!from || !to) return null;
              const x1 = from.x + NODE_W;
              const y1 = from.y + NODE_H / 2;
              const x2 = to.x;
              const y2 = to.y + NODE_H / 2;
              const mid = (x1 + x2) / 2;
              const on = selected !== null && edgeActive(e);
              return (
                <path
                  key={`${e.from}-${e.to}`}
                  d={`M${x1},${y1} C${mid},${y1} ${mid},${y2} ${x2},${y2}`}
                  className={`pm-edge ${e.choice ? 'choice' : ''} ${on ? 'on' : ''}`}
                  opacity={edgeActive(e) ? 1 : 0.15}
                  markerEnd="url(#pm-arrow)"
                />
              );
            })}

            {graph.nodes.map((n) => {
              const pos = positions.get(n.course.code);
              if (!pos) return null;
              const code = n.course.code;
              const title = n.course.title.replace(/^Mason Core: /, '');
              return (
                <g
                  key={code}
                  transform={`translate(${pos.x},${pos.y})`}
                  className={`pm-node ${status.get(code) ?? ''} ${selected === code ? 'sel' : ''}`}
                  opacity={nodeActive(code) ? 1 : 0.25}
                  role="button"
                  tabIndex={0}
                  onClick={() => setSelected(selected === code ? null : code)}
                  onKeyDown={(ev) => {
                    if (ev.key === 'Enter' || ev.key === ' ') {
                      ev.preventDefault();
                      setSelected(selected === code ? null : code);
                    }
                  }}
                >
                  <title>{`${code}: ${n.course.title}`}</title>
                  <rect width={NODE_W} height={NODE_H} rx={8} />
                  <text x={10} y={21} className="pm-code">{code}</text>
                  <text x={10} y={38} className="pm-title">{shorten(title, 22)}</text>
                </g>
              );
            })}
          </svg>
        </div>
      )}

      {selectedCourse && (
        <div className="panel" style={{ marginTop: '1rem' }}>
          <h2>
            {selectedCourse.code} · {selectedCourse.credits} credits
          </h2>
          <p>{selectedCourse.title}</p>
          <p>
            <strong>Catalog rule:</strong> {selectedCourse.prereqText ?? 'None listed.'}
          </p>
          {needs.length > 0 && (
            <p>
              <strong>Needs first:</strong>{' '}
              {needs.map((g) => (g.length > 1 ? `one of ${g.join(' / ')}` : g[0])).join(', ')}
            </p>
          )}
          {unlocks.length > 0 && (
            <p>
              <strong>Unlocks:</strong> {unlocks.join(', ')}
              {descendants.size > unlocks.length &&
                ` (${descendants.size} courses depend on it in total)`}
            </p>
          )}
        </div>
      )}

      {graph.standalone.length > 0 && (
        <>
          <h2>No prerequisite links in this plan</h2>
          <p>
            {graph.standalone.map((c) => (
              <span key={c.id} className={`chip ${status.get(c.code) === 'COMPLETED' ? 'chip-done' : ''}`}>
                {c.code}
              </span>
            ))}
          </p>
        </>
      )}

      <p className="note">
        Only prerequisites between courses in your degree plan are drawn. The catalog rule shown
        for each course can include more, such as alternatives outside your plan. Placeholder
        courses (for example MCORE LIT) stand for any approved course in that category.
      </p>
    </>
  );
}