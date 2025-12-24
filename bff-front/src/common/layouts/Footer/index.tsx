import { useI18n } from "@hooks/useI18n";
import {
  Facebook,
  Twitter,
  Instagram,
  Mail,
  MapPin,
  Phone,
} from "lucide-react";
import { Link } from "react-router-dom";

const Footer = () => {
  const { t } = useI18n();
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gradient-to-b from-secondary-800 to-secondary-900 text-white relative overflow-hidden">
      {/* Decorative top border */}
      <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-primary via-accent to-primary" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12">
          {/* Brand Section */}
          <div className="lg:col-span-1">
            <Link to="/" className="inline-block mb-6">
              <span className="text-2xl font-extrabold font-[family-name:var(--font-heading)] bg-gradient-to-r from-primary-300 to-accent-400 bg-clip-text text-transparent">
                DealToGether
              </span>
            </Link>
            <p className="text-secondary-300 text-sm leading-relaxed mb-6">
              {t("footer.description")}
            </p>
            <div className="flex gap-3">
              <a
                href="#"
                className="p-2.5 bg-white/10 rounded-xl hover:bg-primary hover:scale-110 transition-all duration-300"
                aria-label="Facebook"
              >
                <Facebook className="w-5 h-5" />
              </a>
              <a
                href="#"
                className="p-2.5 bg-white/10 rounded-xl hover:bg-primary hover:scale-110 transition-all duration-300"
                aria-label="Twitter"
              >
                <Twitter className="w-5 h-5" />
              </a>
              <a
                href="#"
                className="p-2.5 bg-white/10 rounded-xl hover:bg-primary hover:scale-110 transition-all duration-300"
                aria-label="Instagram"
              >
                <Instagram className="w-5 h-5" />
              </a>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-lg font-bold font-[family-name:var(--font-heading)] mb-6 text-white">
              {t("footer.links")}
            </h3>
            <ul className="space-y-3">
              {[
                { label: t("footer.privacy"), href: "#" },
                { label: t("footer.terms"), href: "#" },
                { label: t("footer.contact"), href: "#" },
                { label: "FAQ", href: "#" },
              ].map((link, i) => (
                <li key={i}>
                  <a
                    href={link.href}
                    className="text-secondary-300 hover:text-primary-300 transition-colors duration-300 text-sm inline-flex items-center gap-2 group"
                  >
                    <span className="w-1.5 h-1.5 bg-primary/50 rounded-full group-hover:bg-primary transition-colors duration-300" />
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Categories */}
          <div>
            <h3 className="text-lg font-bold font-[family-name:var(--font-heading)] mb-6 text-white">
              Catégories
            </h3>
            <ul className="space-y-3">
              {[
                "Restaurants",
                "Beauté & Spa",
                "Sport & Fitness",
                "Shopping",
                "Voyages",
              ].map((cat, i) => (
                <li key={i}>
                  <a
                    href="#"
                    className="text-secondary-300 hover:text-primary-300 transition-colors duration-300 text-sm inline-flex items-center gap-2 group"
                  >
                    <span className="w-1.5 h-1.5 bg-primary/50 rounded-full group-hover:bg-primary transition-colors duration-300" />
                    {cat}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h3 className="text-lg font-bold font-[family-name:var(--font-heading)] mb-6 text-white">
              Contact
            </h3>
            <ul className="space-y-4">
              <li className="flex items-start gap-3 text-secondary-300 text-sm">
                <MapPin className="w-5 h-5 text-primary-400 shrink-0 mt-0.5" />
                <span>Douala, Cameroun</span>
              </li>
              <li className="flex items-center gap-3 text-secondary-300 text-sm">
                <Phone className="w-5 h-5 text-primary-400 shrink-0" />
                <span>+237 6XX XXX XXX</span>
              </li>
              <li className="flex items-center gap-3 text-secondary-300 text-sm">
                <Mail className="w-5 h-5 text-primary-400 shrink-0" />
                <span>contact@DealToGether.cm</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="border-t border-white/10 mt-12 pt-8">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            <p className="text-secondary-400 text-sm">
              &copy; {currentYear} {t("app.name")}. {t("footer.rights")}
            </p>
            <div className="flex items-center gap-6">
              <a
                href="#"
                className="text-secondary-400 hover:text-primary-300 text-sm transition-colors duration-300"
              >
                Politique de confidentialité
              </a>
              <a
                href="#"
                className="text-secondary-400 hover:text-primary-300 text-sm transition-colors duration-300"
              >
                Mentions légales
              </a>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
