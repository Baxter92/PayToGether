import { Link } from "react-router-dom";
import { ChevronRight, Clock3, Mail } from "lucide-react";
import { PATHS } from "@/common/constants/path";
import { useI18n } from "@hooks/useI18n";

type FooterPageKey =
  | "about"
  | "mission"
  | "howItWorks"
  | "faq"
  | "refunds"
  | "terms"
  | "privacy"
  | "cookies"
  | "legalNotice"
  | "becomeSeller"
  | "sellerTerms"
  | "supplierCharter";

type FooterPageSection = {
  title: string;
  paragraphs?: string[];
  bullets?: string[];
};

type FooterContentPageProps = {
  page: FooterPageKey;
};

export default function FooterContentPage({
  page,
}: FooterContentPageProps) {
  const { t } = useI18n("footerPages");
  const title = t(`${page}.title`);
  const eyebrow = t(`${page}.eyebrow`);
  const intro = t(`${page}.intro`);
  const lastUpdated = t(`${page}.lastUpdated`, { defaultValue: "" });
  const sections = t(`${page}.sections`, {
    returnObjects: true,
    defaultValue: [],
  }) as FooterPageSection[];

  return (
    <div className="relative overflow-hidden bg-[linear-gradient(180deg,#f7f4ed_0%,#fbfaf7_18%,#ffffff_42%,#f6f8fb_100%)]">
      <div className="pointer-events-none absolute inset-x-0 top-0 h-[420px] bg-[radial-gradient(circle_at_top_left,rgba(194,120,59,0.18),transparent_42%),radial-gradient(circle_at_top_right,rgba(15,23,42,0.09),transparent_36%)]" />
      <div className="pointer-events-none absolute left-[8%] top-32 hidden h-40 w-40 rounded-full border border-primary/15 xl:block" />
      <div className="pointer-events-none absolute right-[10%] top-52 hidden h-24 w-24 rounded-full bg-primary/8 blur-2xl xl:block" />

      <section className="relative border-b border-border/40">
        <div className="mx-auto max-w-5xl px-4 py-20 sm:px-6 lg:px-8">
          <div className="grid gap-10 lg:grid-cols-[minmax(0,1fr)_220px] lg:items-end">
            <div className="flex flex-col gap-6">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Link to={PATHS.HOME} className="transition-colors hover:text-primary">
              DealTogether
            </Link>
            <ChevronRight className="h-4 w-4" />
            <span>{title}</span>
          </div>

              <div className="max-w-3xl space-y-5">
                <p className="text-xs font-semibold uppercase tracking-[0.3em] text-primary/80">
                  {eyebrow}
                </p>
                <h1 className="max-w-3xl font-[family-name:var(--font-heading)] text-5xl font-black tracking-[-0.03em] text-foreground sm:text-6xl">
                  {title}
                </h1>
                <p className="max-w-2xl text-lg leading-8 text-muted-foreground sm:text-xl">
                  {intro}
                </p>
              </div>

              {lastUpdated ? (
                <div className="inline-flex w-fit items-center gap-2 rounded-full border border-primary/10 bg-white/80 px-4 py-2 text-sm text-muted-foreground shadow-sm backdrop-blur">
                  <Clock3 className="h-4 w-4 text-primary" />
                  <span>
                    {t("common.lastUpdatedLabel")}: {lastUpdated}
                  </span>
                </div>
              ) : null}
            </div>

            <div className="rounded-[32px] border border-border/60 bg-white/70 p-6 shadow-[0_20px_60px_-40px_rgba(15,23,42,0.35)] backdrop-blur">
              <div className="space-y-4">
                <div className="h-px w-12 bg-primary/40" />
                <p className="font-[family-name:var(--font-heading)] text-lg font-bold text-foreground">
                  {title}
                </p>
                <p className="text-sm leading-7 text-muted-foreground">
                  {sections.length} section{sections.length > 1 ? "s" : ""}
                </p>
                <ol className="space-y-2 text-sm text-muted-foreground">
                  {sections.slice(0, 4).map((section, index) => (
                    <li key={section.title} className="flex items-start gap-3">
                      <span className="mt-0.5 inline-flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
                        {index + 1}
                      </span>
                      <span className="leading-6">{section.title}</span>
                    </li>
                  ))}
                </ol>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="relative mx-auto max-w-5xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="space-y-12">
          {sections.map((section, index) => (
            <article
              key={section.title}
              className={`grid gap-6 border-b border-border/45 pb-12 last:border-b-0 last:pb-0 md:grid-cols-[180px_minmax(0,1fr)] lg:grid-cols-[220px_minmax(0,1fr)] ${
                index % 2 === 0 ? "" : ""
              }`}
            >
              <div className="pt-1">
                <div className="sticky top-24">
                  <div className="mb-4 h-px w-12 bg-primary/35" />
                  <p className="text-xs font-semibold uppercase tracking-[0.24em] text-primary/70">
                    Section {index + 1}
                  </p>
                </div>
              </div>

              <div className="max-w-3xl">
                <h2 className="font-[family-name:var(--font-heading)] text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
                  {section.title}
                </h2>

                {section.paragraphs?.length ? (
                  <div className="mt-6 space-y-4 text-[1.02rem] leading-8 text-muted-foreground">
                    {section.paragraphs.map((paragraph) => (
                      <p key={paragraph}>{paragraph}</p>
                    ))}
                  </div>
                ) : null}

                {section.bullets?.length ? (
                  <ul className="mt-8 w-full max-w-3xl space-y-4 border-l border-primary/15 pl-5 text-base text-muted-foreground sm:pl-8">
                    {section.bullets.map((bullet) => (
                      <li
                        key={bullet}
                        className="flex w-full items-start gap-3 rounded-2xl bg-white/55 px-4 py-3 leading-7 sm:px-5"
                      >
                        <span className="mt-2.5 h-2 w-2 shrink-0 rounded-full bg-primary/70" />
                        <span className="min-w-0 flex-1">{bullet}</span>
                      </li>
                    ))}
                  </ul>
                ) : null}
              </div>
            </article>
          ))}
        </div>

        <div className="mt-16 border-t border-border/60 pt-10">
          <div className="relative overflow-hidden rounded-[32px] bg-[linear-gradient(135deg,#0f172a_0%,#172033_45%,#24324d_100%)] px-6 py-8 text-white shadow-[0_30px_80px_-45px_rgba(15,23,42,0.7)] sm:px-8">
            <div className="pointer-events-none absolute right-0 top-0 h-32 w-32 rounded-full bg-white/8 blur-2xl" />
            <div className="pointer-events-none absolute bottom-0 left-0 h-24 w-24 rounded-full bg-primary/20 blur-2xl" />
            <div className="relative flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
            <div className="space-y-2">
              <p className="font-[family-name:var(--font-heading)] text-xl font-bold">
                {t("common.contactTitle")}
              </p>
              <p className="max-w-xl text-sm leading-7 text-white/70">
                {t("common.contactDescription")}
              </p>
            </div>
            <a
              href="mailto:contact@dealtogether.ca"
              className="inline-flex items-center gap-2 rounded-full bg-white px-5 py-3 text-sm font-semibold text-secondary-900 transition-colors hover:bg-white/90"
            >
              <Mail className="h-4 w-4" />
              contact@dealtogether.ca
            </a>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
