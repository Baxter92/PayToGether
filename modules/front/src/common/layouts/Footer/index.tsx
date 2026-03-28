import { Mail, MapPin, Phone } from "lucide-react";
import { Link } from "react-router-dom";
import { useI18n } from "@hooks/useI18n";
import { PATHS } from "@/common/constants/path";

type FooterLink = {
  label: string;
  to?: string;
  href?: string;
};

const Footer = () => {
  const { t: tFooter } = useI18n("footer");
  const { t: tApp } = useI18n("app");
  const currentYear = new Date().getFullYear();

  const sections: { title: string; links: FooterLink[] }[] = [
    {
      title: tFooter("sections.company.title"),
      links: [
        { label: tFooter("sections.company.about"), to: PATHS.ABOUT },
        { label: tFooter("sections.company.mission"), to: PATHS.MISSION },
        {
          label: tFooter("sections.company.contact"),
          href: "mailto:contact@dealtogether.ca",
        },
      ],
    },
    {
      title: tFooter("sections.buyers.title"),
      links: [
        { label: tFooter("sections.buyers.howItWorks"), to: PATHS.HOW_IT_WORKS },
        { label: tFooter("sections.buyers.faq"), to: PATHS.FAQ },
        { label: tFooter("sections.buyers.refunds"), to: PATHS.REFUNDS },
      ],
    },
    {
      title: tFooter("sections.sellers.title"),
      links: [
        {
          label: tFooter("sections.sellers.becomeSeller"),
          to: PATHS.BECOME_SELLER,
        },
        { label: tFooter("sections.sellers.sellerTerms"), to: PATHS.SELLER_TERMS },
        {
          label: tFooter("sections.sellers.supplierCharter"),
          to: PATHS.SUPPLIER_CHARTER,
        },
      ],
    },
    {
      title: tFooter("sections.legal.title"),
      links: [
        { label: tFooter("sections.legal.terms"), to: PATHS.TERMS },
        { label: tFooter("sections.legal.privacy"), to: PATHS.PRIVACY },
        { label: tFooter("sections.legal.cookies"), to: PATHS.COOKIES },
        {
          label: tFooter("sections.legal.legalNotice"),
          to: PATHS.LEGAL_NOTICE,
        },
      ],
    },
  ];

  return (
    <footer className="relative overflow-hidden bg-gradient-to-b from-secondary-800 to-secondary-900 text-white">
      <div className="absolute left-0 right-0 top-0 h-1 bg-gradient-to-r from-primary via-accent to-primary" />

      <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-12 lg:grid-cols-[1.2fr_4fr]">
          <div className="max-w-sm">
            <Link to="/" className="mb-4 inline-block">
              <span className="bg-gradient-to-r from-primary-300 to-accent-400 bg-clip-text font-[family-name:var(--font-heading)] text-2xl font-extrabold text-transparent">
                {tApp("name")}
              </span>
            </Link>
            <p className="mb-6 text-sm leading-relaxed text-secondary-300">
              {tFooter("description")}
            </p>
            <ul className="space-y-3 text-sm text-secondary-300">
              <li className="flex items-start gap-3">
                <MapPin className="mt-0.5 h-5 w-5 shrink-0 text-primary-400" />
                <span>{tFooter("contact.location")}</span>
              </li>
              <li className="flex items-center gap-3">
                <Phone className="h-5 w-5 shrink-0 text-primary-400" />
                <span>{tFooter("contact.phone")}</span>
              </li>
              <li className="flex items-center gap-3">
                <Mail className="h-5 w-5 shrink-0 text-primary-400" />
                <span>{tFooter("contact.email")}</span>
              </li>
            </ul>
          </div>

          <div className="grid grid-cols-1 gap-10 sm:grid-cols-2 xl:grid-cols-5">
            {sections.map((section) => (
              <div key={section.title}>
                <h3 className="mb-4 font-[family-name:var(--font-heading)] text-lg font-bold text-white">
                  {section.title}
                </h3>
                <ul className="space-y-3">
                  {section.links.map((link) => (
                    <li key={link.label}>
                      {link.to ? (
                        <Link
                          to={link.to}
                          className="inline-flex items-center gap-2 text-sm text-secondary-300 transition-colors duration-300 hover:text-primary-300"
                        >
                          <span className="h-1.5 w-1.5 rounded-full bg-primary/50" />
                          {link.label}
                        </Link>
                      ) : link.href ? (
                        <a
                          href={link.href}
                          className="inline-flex items-center gap-2 text-sm text-secondary-300 transition-colors duration-300 hover:text-primary-300"
                        >
                          <span className="h-1.5 w-1.5 rounded-full bg-primary/50" />
                          {link.label}
                        </a>
                      ) : (
                        <span className="inline-flex items-center gap-2 text-sm text-secondary-500">
                          <span className="h-1.5 w-1.5 rounded-full bg-white/20" />
                          {link.label}
                        </span>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>

        <div className="mt-12 border-t border-white/10 pt-8">
          <div className="flex flex-col items-center justify-between gap-4 md:flex-row">
            <p className="text-sm text-secondary-400">
              &copy; {currentYear} {tApp("name")}. {tFooter("rights")}
            </p>
            <div className="flex items-center gap-6">
              <Link
                to={PATHS.PRIVACY}
                className="text-sm text-secondary-400 transition-colors duration-300 hover:text-primary-300"
              >
                {tFooter("sections.legal.privacy")}
              </Link>
              <Link
                to={PATHS.LEGAL_NOTICE}
                className="text-sm text-secondary-400 transition-colors duration-300 hover:text-primary-300"
              >
                {tFooter("sections.legal.legalNotice")}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
