import type { ReactElement } from "react";
import { Save } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import { Separator } from "@/common/components/ui/separator";
import Form from "@/common/containers/Form";
import { z } from "zod";
import { toast } from "sonner";

const generalSettingsSchema = z.object({
  siteName: z.string().min(1, "Nom du site requis"),
  siteDescription: z.string().min(1, "Description requise"),
  contactEmail: z.string().email("Email invalide"),
});

const commissionSettingsSchema = z.object({
  commissionRate: z.number().min(0).max(100),
  minCommission: z.number().min(0),
});

const notificationSettingsSchema = z.object({
  smtpHost: z.string().min(1, "Serveur SMTP requis"),
  smtpPort: z.number().min(1).max(65535),
  smtpUser: z.string().min(1, "Utilisateur requis"),
});

export default function AdminSettings(): ReactElement {
  const handleGeneralSubmit = async ({
    data,
  }: {
    data: z.infer<typeof generalSettingsSchema>;
  }) => {
    console.log("General settings:", data);
    toast.success("Paramètres généraux enregistrés");
  };

  const handleCommissionSubmit = async ({
    data,
  }: {
    data: z.infer<typeof commissionSettingsSchema>;
  }) => {
    console.log("Commission settings:", data);
    toast.success("Commissions enregistrées");
  };

  const handleNotificationSubmit = async ({
    data,
  }: {
    data: z.infer<typeof notificationSettingsSchema>;
  }) => {
    console.log("Notification settings:", data);
    toast.success("Paramètres de notification enregistrés");
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold font-heading">Paramètres</h1>
        <p className="text-muted-foreground">
          Configurez les paramètres de votre plateforme
        </p>
      </div>

      <div className="grid gap-6">
        {/* General Settings */}
        <Card>
          <CardHeader>
            <CardTitle>Paramètres généraux</CardTitle>
            <CardDescription>
              Informations de base de votre plateforme
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Form
              schema={generalSettingsSchema}
              defaultValues={{
                siteName: "DealGroup",
                siteDescription: "Plateforme d'achats groupés",
                contactEmail: "contact@dealgroup.fr",
              }}
              onSubmit={handleGeneralSubmit}
              showResetButton={false}
              submitLabel="Enregistrer"
              submitBtnProps={{
                leftIcon: <Save className="h-4 w-4" />,
              }}
              fields={[
                {
                  name: "siteName",
                  label: "Nom du site",
                  type: "text",
                  placeholder: "DealGroup",
                },
                {
                  name: "siteDescription",
                  label: "Description",
                  type: "text",
                  placeholder: "Description de votre site",
                },
                {
                  name: "contactEmail",
                  label: "Email de contact",
                  type: "email",
                  placeholder: "contact@example.com",
                },
              ]}
            />
          </CardContent>
        </Card>

        {/* Commission Settings */}
        <Card>
          <CardHeader>
            <CardTitle>Commissions</CardTitle>
            <CardDescription>
              Configurez les taux de commission sur les ventes
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Form
              schema={commissionSettingsSchema}
              defaultValues={{
                commissionRate: 10,
                minCommission: 1,
              }}
              onSubmit={handleCommissionSubmit}
              showResetButton={false}
              submitLabel="Enregistrer"
              submitBtnProps={{
                leftIcon: <Save className="h-4 w-4" />,
              }}
              columns={2}
              fields={[
                {
                  name: "commissionRate",
                  label: "Taux de commission (%)",
                  type: "number",
                  placeholder: "10",
                },
                {
                  name: "minCommission",
                  label: "Commission minimum",
                  type: "number",
                  placeholder: "1",
                },
              ]}
            />
          </CardContent>
        </Card>

        {/* Notification Settings */}
        <Card>
          <CardHeader>
            <CardTitle>Notifications</CardTitle>
            <CardDescription>
              Paramètres des notifications email
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Form
              schema={notificationSettingsSchema}
              defaultValues={{
                smtpHost: "smtp.example.com",
                smtpPort: 587,
                smtpUser: "noreply@dealgroup.fr",
              }}
              onSubmit={handleNotificationSubmit}
              showResetButton={false}
              submitLabel="Enregistrer"
              submitBtnProps={{
                leftIcon: <Save className="h-4 w-4" />,
              }}
              columns={3}
              fields={[
                {
                  name: "smtpHost",
                  label: "Serveur SMTP",
                  type: "text",
                  placeholder: "smtp.example.com",
                },
                {
                  name: "smtpPort",
                  label: "Port",
                  type: "number",
                  placeholder: "587",
                },
                {
                  name: "smtpUser",
                  label: "Utilisateur",
                  type: "text",
                  placeholder: "user@example.com",
                },
              ]}
            />
          </CardContent>
        </Card>

        <Separator />
      </div>
    </div>
  );
}
