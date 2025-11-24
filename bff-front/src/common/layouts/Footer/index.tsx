import { useI18n } from "@/common/context/I18nContext";
import { Button } from "../../components/ui/button";
import { Facebook, Twitter } from "lucide-react";

const Footer = () => {
  const { t } = useI18n();
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-800 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Section 1 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">{t("footer.about")}</h3>
            <p className="text-gray-400 text-sm">{t("footer.description")}</p>
          </div>

          {/* Section 2 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">{t("footer.links")}</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition"
                >
                  {t("footer.privacy")}
                </a>
              </li>
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition"
                >
                  {t("footer.terms")}
                </a>
              </li>
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition"
                >
                  {t("footer.contact")}
                </a>
              </li>
            </ul>
          </div>

          {/* Section 3 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">{t("footer.social")}</h3>
            <div className="flex gap-4">
              <a>
                <Facebook className="w-5 h-5 fill-current" />
              </a>
              <a>
                <Twitter className="w-5 h-5 fill-current" />
              </a>
            </div>
          </div>
        </div>

        <div className="border-t border-gray-700 mt-8 pt-8 text-center text-sm text-gray-400">
          <p>
            &copy; {currentYear} {t("app.name")}. {t("footer.rights")}
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
