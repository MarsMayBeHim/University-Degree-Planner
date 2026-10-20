import { useQuery } from '@tanstack/react-query';
import { getProgress } from '../api/progress';
import GroupCard from '../components/GroupCard';
import ProgressBar from '../components/ProgressBar';

export default function DashboardPage() {
  const progress = useQuery({ queryKey: ['progress'], queryFn: getProgress });

  if (progress.isLoading) return <p>Loading...</p>;
  if (progress.isError || !progress.data) {
    return <p className="warn">Could not load your degree progress.</p>;
  }

  const p = progress.data;

  return (
    <>
      <h1>Degree Progress</h1>
      <p>
        {p.programName} · catalog year {p.catalogYear}
      </p>

      <div className="panel">
        <ProgressBar
          value={p.creditsCompleted / p.totalCreditsRequired}
          label={`${p.creditsCompleted} of ${p.totalCreditsRequired} credits completed`}
        />
        {p.creditsPlanned > 0 && <p>{p.creditsPlanned} more credits are planned or in progress.</p>}
      </div>

      <h2>Requirements</h2>
      {p.groups.map((g) => (
        <GroupCard key={g.id} group={g} />
      ))}

      <p className="note">
        Natural science, most of Mason Core, CS-related electives and free electives are not
        tracked yet, so finishing everything above does not mean the degree is complete. This
        planner assumes you already meet all grade and admission requirements. Confirm your
        plan with your advisor.
      </p>
    </>
  );
}