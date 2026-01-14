import { useI18n } from "@hooks/useI18n";
import Form, { type IFieldConfig } from "@/common/containers/Form";
import { Heading } from "@/common/containers/Heading";
import { useAuth } from "@/common/context/AuthContext";
import * as z from "zod";

export default function Settings() {
  const { t } = useI18n("profile");
  const { user } = useAuth();

  const fields: IFieldConfig[] = [
    {
      name: "email",
      label: t("profile.emailLabel"),
      type: "email",
      placeholder: "Email",
      readOnly: true,
    },
    {
      name: "firstName",
      label: t("profile.firstNameLabel"),
      type: "text",
      placeholder: t("profile.firstNameLabel"),
    },
    {
      name: "lastName",
      label: t("profile.lastNameLabel"),
      type: "text",
      placeholder: t("profile.lastNameLabel"),
    },
    {
      name: "phone",
      label: t("profile.phoneLabel"),
      type: "text",
      placeholder: t("profile.phoneLabel"),
    },
    {
      name: "password",
      label: t("profile.changePassword"),
      type: "password",
      placeholder: t("auth.password"),
    },
  ];

  return (
    <section>
      <Heading
        level={2}
        title={t("profile.settingsTitle")}
        description={t("profile.settingsDescription")}
        underline
      />
      <Form
        fields={fields}
        schema={z.object({
          email: z.email(),
          firstName: z.string(),
          lastName: z.string(),
          phone: z.string(),
          password: z.string().optional(),
        })}
        defaultValues={{
          email: user?.email,
          firstName: user?.name,
          lastName: user?.name,
        }}
        onSubmit={(data) => console.log(data)}
      />
    </section>
  );
}
