type Props = {
  value: number; // 0 to 1
  label?: string;
};

export default function ProgressBar({ value, label }: Props) {
  const percent = Math.round(Math.min(Math.max(value, 0), 1) * 100);
  return (
    <div>
      <div className="bar">
        <div className="bar-fill" style={{ width: `${percent}%` }} />
      </div>
      {label && <small>{label}</small>}
    </div>
  );
}