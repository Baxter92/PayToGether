export function timeAgo(isoDate: string): string {
  const _isoDate = isoDate.endsWith("Z") ? isoDate : `${isoDate}Z`;
  const d = new Date(_isoDate);
  const now = new Date();
  const diff = Math.floor((now.getTime() - d.getTime()) / 1000);
  if (diff < 60) return `${diff}s`;
  if (diff < 3600) return `${Math.floor(diff / 60)}m`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h`;
  return `${Math.floor(diff / 86400)}j`;
}
