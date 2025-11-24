import { slides } from "@/common/constants/data";

import Features from "./containers/Features";
import Hero from "@containers/Hero";
import type { IFormContainerConfig } from "@/common/containers/Form";
import * as z from "zod";
import Form from "@/common/containers/Form";
import type { JSX } from "react";

export default function Home(): JSX.Element {
  const validationSchema = z
    .object({
      nom: z.string().min(2, "Le nom doit contenir au moins 2 caractères"),
      prenom: z
        .string()
        .min(2, "Le prénom doit contenir au moins 2 caractères"),
      email: z.string().email("Email invalide"),
      age: z
        .number()
        .min(18, "Vous devez avoir au moins 18 ans")
        .max(120, "Âge invalide"),
      pays: z.string().min(1, "Veuillez sélectionner un pays"),
      newsletter: z.boolean().optional(),
      contact: z.string().optional(),
      message: z.string().optional(),
    })
    .refine(
      (data) => {
        // Validation personnalisée : si newsletter est coché, contact doit être rempli
        if (data.newsletter && !data.contact) {
          return false;
        }
        return true;
      },
      {
        message:
          "Si vous souhaitez recevoir la newsletter, veuillez choisir un moyen de contact",
        path: ["contact"],
      }
    );

  const formConfig: Omit<IFormContainerConfig, "schema"> = {
    groups: [
      {
        title: "Informations personnelles",
        description: "Renseignez vos informations de base",
        columns: 2,
        fields: [
          {
            name: "nom",
            label: "Nom",
            type: "text",
            placeholder: "Votre nom",
          },
          {
            name: "prenom",
            label: "Prénom",
            type: "text",
            placeholder: "Votre prénom",
          },
          {
            name: "email",
            label: "Email",
            type: "email",
            placeholder: "exemple@email.com",
          },
          {
            name: "age",
            label: "Âge",
            type: "number",
          },
          {
            name: "pays",
            label: "Pays",
            type: "select",
            items: [
              { label: "France", value: "fr" },
              { label: "Cameroun", value: "cm" },
              { label: "Canada", value: "ca" },
            ],
          },
        ],
      },
      {
        title: "Préférences",
        columns: 1,
        fields: [
          {
            name: "newsletter",
            label: "Je souhaite recevoir la newsletter",
            type: "checkbox",
          },
          {
            name: "contact",
            label: "Moyen de contact préféré",
            type: "radio",
            items: [
              { label: "Email", value: "email" },
              { label: "Téléphone", value: "phone" },
              { label: "SMS", value: "sms" },
            ],
          },
          {
            name: "message",
            label: "Message",
            type: "textarea",
            placeholder: "Votre message...",
          },
        ],
      },
    ],
    onSubmit: (data) => {
      console.log("Données du formulaire:", data);
      alert("Formulaire soumis avec succès!");
    },
  };

  return (
    <div className="mx-auto">
      <Hero slides={slides} />
      <Features />
      <Form {...formConfig} schema={validationSchema} />
    </div>
  );
}
