import Form, { type IFieldConfig } from "@/common/containers/Form";
import { Heading } from "@/common/containers/Heading";
import { useAuth } from "@/common/context/AuthContext";
import * as z from "zod";

export default function Settings() {
  const { user } = useAuth();
  const fields: IFieldConfig[] = [
    {
      name: "email",
      label: "Adresse e-mail",
      type: "email",
      placeholder: "Email",
      readOnly: true,
    },
    {
      name: "firstName",
      label: "Prénom",
      type: "text",
      placeholder: "Prénom",
    },
    {
      name: "lastName",
      label: "Nom",
      type: "text",
      placeholder: "Nom",
    },
    {
      name: "phone",
      label: "Téléphone",
      type: "text",
      placeholder: "Téléphone",
    },
    {
      name: "password",
      label: "Modifier le mot de passe",
      type: "password",
      placeholder: "Mot de passe",
    },
  ];
  return (
    <section>
      <Heading
        level={2}
        title="Paramètres"
        description="Gérez vos paramètres de compte."
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
