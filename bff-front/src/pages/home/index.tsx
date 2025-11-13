import { useI18n } from "@/context/I18nContext";

export default function Home() {
  const { t } = useI18n();
  return <div className="mx-auto">{t("hello")}</div>;
}
