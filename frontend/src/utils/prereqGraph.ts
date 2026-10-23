import type { PrereqCourse, PrereqNode } from '../types';

export type Edge = { from: string; to: string; choice: boolean };
export type PlacedNode = { course: PrereqCourse; layer: number; row: number };
export type Graph = {
  nodes: PlacedNode[];
  edges: Edge[];
  standalone: PrereqCourse[];
  layerCount: number;
  rowCount: number;
};

function leafCodes(node: PrereqNode): string[] {
  if (node.type === 'COURSE') return node.code ? [node.code] : [];
  return node.children.flatMap(leafCodes);
}

/**
 * Splits a prerequisite tree into groups. Every group must be satisfied,
 * and a group with several codes means "one of these".
 */
export function requirementGroups(tree: PrereqNode | null): string[][] {
  if (!tree) return [];
  if (tree.type === 'COURSE') return tree.code ? [[tree.code]] : [];
  if (tree.type === 'AND') return tree.children.flatMap(requirementGroups);
  return [leafCodes(tree)];
}

export function buildGraph(courses: PrereqCourse[]): Graph {
  const known = new Set(courses.map((c) => c.code));

  // Only prerequisites that are part of this degree plan become edges
  const edges: Edge[] = [];
  for (const course of courses) {
    for (const group of requirementGroups(course.prereqTree)) {
      const choice = group.length > 1;
      for (const code of group) {
        if (known.has(code) && code !== course.code) {
          edges.push({ from: code, to: course.code, choice });
        }
      }
    }
  }

  const incoming = new Map<string, string[]>();
  const outgoing = new Map<string, string[]>();
  for (const e of edges) {
    incoming.set(e.to, [...(incoming.get(e.to) ?? []), e.from]);
    outgoing.set(e.from, [...(outgoing.get(e.from) ?? []), e.to]);
  }

  const connected = courses.filter((c) => incoming.has(c.code) || outgoing.has(c.code));
  const standalone = courses.filter((c) => !incoming.has(c.code) && !outgoing.has(c.code));

  // Layer = length of the longest chain of prerequisites leading to the course
  const layerOf = new Map<string, number>();
  function layer(code: string, visiting: Set<string>): number {
    const cached = layerOf.get(code);
    if (cached !== undefined) return cached;
    if (visiting.has(code)) return 0;
    visiting.add(code);
    const parents = incoming.get(code) ?? [];
    const value = parents.length === 0 ? 0 : 1 + Math.max(...parents.map((p) => layer(p, visiting)));
    visiting.delete(code);
    layerOf.set(code, value);
    return value;
  }
  connected.forEach((c) => layer(c.code, new Set()));

  const layerCount =
    connected.length === 0 ? 0 : Math.max(...connected.map((c) => layerOf.get(c.code) ?? 0)) + 1;

  // Order each column so connected courses sit near their prerequisites
  const rowOf = new Map<string, number>();
  const nodes: PlacedNode[] = [];
  let rowCount = 0;
  for (let l = 0; l < layerCount; l++) {
    const inLayer = connected.filter((c) => layerOf.get(c.code) === l);
    const score = (c: PrereqCourse): number => {
      const parents = (incoming.get(c.code) ?? []).filter((p) => rowOf.has(p));
      if (parents.length === 0) return -(outgoing.get(c.code)?.length ?? 0);
      return parents.reduce((sum, p) => sum + (rowOf.get(p) ?? 0), 0) / parents.length;
    };
    inLayer.sort((a, b) => score(a) - score(b) || a.code.localeCompare(b.code));
    inLayer.forEach((c, i) => {
      rowOf.set(c.code, i);
      nodes.push({ course: c, layer: l, row: i });
    });
    rowCount = Math.max(rowCount, inLayer.length);
  }

  return { nodes, edges, standalone, layerCount, rowCount };
}

function reach(start: string, edges: Edge[], direction: 'up' | 'down'): Set<string> {
  const seen = new Set<string>();
  const queue = [start];
  while (queue.length > 0) {
    const current = queue.pop() as string;
    for (const e of edges) {
      const [near, far] = direction === 'up' ? [e.to, e.from] : [e.from, e.to];
      if (near === current && !seen.has(far)) {
        seen.add(far);
        queue.push(far);
      }
    }
  }
  return seen;
}

/** Every course that must come before this one, directly or indirectly. */
export const ancestorsOf = (code: string, edges: Edge[]) => reach(code, edges, 'up');

/** Every course that this one eventually unlocks. */
export const descendantsOf = (code: string, edges: Edge[]) => reach(code, edges, 'down');