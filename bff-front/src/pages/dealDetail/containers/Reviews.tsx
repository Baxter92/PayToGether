import React from "react";
import { Avatar, AvatarFallback } from "@components/ui/avatar";
import { Button } from "@components/ui/button";
import { ChevronRight } from "lucide-react";

export default function Reviews({ count }: { count: number }) {
  const mock = [1, 2, 3].slice(0, Math.min(3, count));
  return (
    <section className="mt-6">
      <h3 className="text-lg font-medium">Avis des clients</h3>
      <div className="mt-3 space-y-4">
        {mock.map((i) => (
          <article key={i} className="border rounded p-4">
            <div className="flex items-start gap-3">
              <Avatar className="rounded-lg">
                <AvatarFallback>U</AvatarFallback>
              </Avatar>
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <div className="text-sm font-medium">Utilisateur {i}</div>
                  <div className="text-sm text-amber-600">4.{i} ★</div>
                </div>
                <p className="mt-2 text-gray-700">
                  Très bon produit, viande de qualité et livraison soignée.
                </p>
              </div>
            </div>
          </article>
        ))}

        <Button
          variant="ghost"
          colorScheme="info"
          leftIcon={<ChevronRight className="w-4 h-4" />}
        >
          Voir tous les avis
        </Button>
      </div>
    </section>
  );
}
