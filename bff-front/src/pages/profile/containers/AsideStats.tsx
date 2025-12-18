import { VStack } from "@/common/components";
import Grid from "@/common/components/Grid";
import { Button } from "@/common/components/ui/button";
import { Heading } from "@/common/containers/Heading";
import { type JSX } from "react";

export default function AsideStats(): JSX.Element {
  return (
    <aside className="space-y-4 lg:col-span-1">
      <VStack
        className="bg-white dark:bg-slate-900 rounded-lg p-4 shadow-sm"
        spacing={10}
      >
        <Heading level={6} title="Compte" underline />
        <Grid cols={{ md: 3, base: 2 }} gap="gap-8" className="mt-2">
          <Heading
            level={6}
            title="10"
            description="Commandes"
            align="center"
            spacing={0}
          />
          <Heading
            level={6}
            title="3"
            description="Favoris"
            align="center"
            spacing={0}
          />
          <Heading
            level={6}
            title="2"
            description="Deals"
            align="center"
            spacing={0}
          />
        </Grid>

        <Button variant="outline">Voir les méthodes de paiement</Button>
      </VStack>

      <div className="bg-white dark:bg-slate-900 rounded-lg p-4 shadow-sm">
        <h4 className="text-sm font-medium">Aide rapide</h4>
        <ul className="text-sm text-slate-600 mt-2 space-y-2">
          <li>• Suivre une commande</li>
          <li>• Contacter un marchand</li>
          <li>• Demander un remboursement</li>
        </ul>
      </div>
    </aside>
  );
}
