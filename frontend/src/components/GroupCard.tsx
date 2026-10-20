import type { GroupProgress } from '../types';
import ProgressBar from './ProgressBar';

function hasProgress(g: GroupProgress): boolean {
  return g.completedCourses.length > 0 || g.children.some(hasProgress);
}

function ratio(g: GroupProgress): number {
  if (g.ruleType === 'MIN_CREDITS' && g.creditsRequired) {
    return g.creditsEarned / g.creditsRequired;
  }
  if (g.coursesRequired) {
    return g.coursesCompleted / g.coursesRequired;
  }
  return g.done ? 1 : 0;
}

function barLabel(g: GroupProgress): string | undefined {
  if (g.ruleType === 'MIN_CREDITS' && g.creditsRequired) {
    return `${g.creditsEarned} / ${g.creditsRequired} credits`;
  }
  if (g.coursesRequired) {
    return `${Math.min(g.coursesCompleted, g.coursesRequired)} / ${g.coursesRequired} courses`;
  }
  return undefined;
}

function rule(g: GroupProgress): string {
  if (g.ruleType === 'ALL') return 'Complete all';
  if (g.ruleType === 'CHOOSE_N') return `Choose ${g.coursesRequired}`;
  return `${g.creditsRequired} credits`;
}

type Props = {
  group: GroupProgress;
  depth?: number;
};

export default function GroupCard({ group, depth = 0 }: Props) {
  const status = group.done ? 'done' : hasProgress(group) ? 'progress' : 'notstarted';
  const statusText = { done: 'Done', progress: 'In progress', notstarted: 'Not started' }[status];

  return (
    <details className={`group ${status}`} open={depth === 0}>
      <summary>
        <span className="group-name">{group.name}</span>
        <span className="group-rule">{rule(group)}</span>
        <span className={`badge ${status}`}>{statusText}</span>
      </summary>

      <div className="group-body">
        <ProgressBar value={ratio(group)} label={barLabel(group)} />

        {group.completedCourses.length > 0 && (
          <p>
            <strong>Completed:</strong>{' '}
            {group.completedCourses.map((code) => (
              <span key={code} className="chip chip-done">{code}</span>
            ))}
          </p>
        )}

        {!group.done && group.remainingCourses.length > 0 && (
          <p>
            <strong>{group.ruleType === 'ALL' ? 'Still needed:' : 'Options remaining:'}</strong>{' '}
            {group.remainingCourses.map((code) => (
              <span key={code} className="chip">{code}</span>
            ))}
          </p>
        )}

        {group.children.map((child) => (
          <GroupCard key={child.id} group={child} depth={depth + 1} />
        ))}
      </div>
    </details>
  );
}