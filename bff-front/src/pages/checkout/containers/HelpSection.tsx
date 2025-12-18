import { HStack } from "@/common/components";
import { Button } from "@/common/components/ui/button";
import { Card, CardContent } from "@/common/components/ui/card";
import type { JSX } from "react";

export interface IHelpSectionProps {
  onBack: () => void;
  onHome: () => void;
}

export default function HelpSection({
  onBack,
  onHome,
}: IHelpSectionProps): JSX.Element {
  return (
    <Card className="border-border bg-card">
      <CardContent className="pt-6">
        <h4 className="font-semibold text-foreground mb-2">Besoin d'aide ?</h4>
        <p className="text-sm text-muted-foreground mb-4">
          Notre équipe de support est disponible pour répondre à vos questions.
        </p>
        <HStack spacing={2}>
          <Button
            variant="outline"
            size="sm"
            onClick={onBack}
            className="flex-1"
          >
            Retour
          </Button>
          <Button variant="ghost" size="sm" onClick={onHome} className="flex-1">
            Accueil
          </Button>
        </HStack>
      </CardContent>
    </Card>
  );
}
