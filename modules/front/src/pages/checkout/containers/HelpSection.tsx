import { useI18n } from "@hooks/useI18n";
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
  const { t } = useI18n("checkout");

  return (
    <Card className="border-border bg-card">
      <CardContent className="pt-6">
        <h4 className="font-semibold text-foreground mb-2">
          {t("checkout.needHelp")}
        </h4>
        <p className="text-sm text-muted-foreground mb-4">
          {t("checkout.helpText")}
        </p>
        <HStack spacing={2}>
          <Button
            variant="outline"
            size="sm"
            onClick={onBack}
            className="flex-1"
          >
            {t("checkout.back")}
          </Button>
          <Button variant="ghost" size="sm" onClick={onHome} className="flex-1">
            {t("nav.home")}
          </Button>
        </HStack>
      </CardContent>
    </Card>
  );
}
