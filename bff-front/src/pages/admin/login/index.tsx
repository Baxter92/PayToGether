import { useState, type ReactElement } from "react";
import { useNavigate } from "react-router-dom";
import { LogIn, ShieldCheck } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import { toast } from "sonner";
import { ADMIN_PATHS } from "../constants/adminPaths";
import Form from "@/common/containers/Form";
import { z } from "zod";

// Simulation - Remplacer par une vraie authentification backend
const MOCK_ADMIN = {
  email: "admin@example.com",
  password: "admin123",
};

const loginSchema = z.object({
  email: z.string().email("Email invalide"),
  password: z.string().min(6, "Mot de passe requis (min 6 caractères)"),
});

type LoginFormData = z.infer<typeof loginSchema>;

export default function AdminLogin(): ReactElement {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async ({ data }: { data: LoginFormData }) => {
    setIsLoading(true);

    // Simulation d'un délai réseau
    await new Promise((resolve) => setTimeout(resolve, 1000));

    if (data.email === MOCK_ADMIN.email && data.password === MOCK_ADMIN.password) {
      sessionStorage.setItem("adminAuthenticated", "true");
      toast.success("Connexion réussie", {
        description: "Bienvenue dans le dashboard admin",
      });
      navigate(ADMIN_PATHS.DASHBOARD);
    } else {
      toast.error("Erreur de connexion", {
        description: "Email ou mot de passe incorrect",
      });
    }

    setIsLoading(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/30 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-4">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-primary/10">
            <ShieldCheck className="h-8 w-8 text-primary" />
          </div>
          <div>
            <CardTitle className="text-2xl font-heading">
              Administration
            </CardTitle>
            <CardDescription>
              Connectez-vous pour accéder au dashboard admin
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent>
          <Form<LoginFormData>
            schema={loginSchema}
            defaultValues={{ email: "", password: "" }}
            onSubmit={handleSubmit}
            showResetButton={false}
            submitLabel="Se connecter"
            submitBtnProps={{
              className: "w-full",
              leftIcon: <LogIn className="h-4 w-4" />,
              loading: isLoading,
            }}
            fields={[
              {
                name: "email",
                label: "Email",
                type: "email",
                placeholder: "admin@example.com",
              },
              {
                name: "password",
                label: "Mot de passe",
                type: "password",
                placeholder: "••••••••",
              }
            ]}
          />

          <div className="mt-6 p-4 bg-muted/50 rounded-lg">
            <p className="text-sm text-muted-foreground text-center">
              <strong>Démo :</strong> admin@example.com / admin123
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
