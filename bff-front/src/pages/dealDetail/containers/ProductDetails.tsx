import React, { useMemo } from "react";
import { Badge } from "@components/ui/badge";
import { Separator } from "@components/ui/separator";
import type { Deal } from "../types";

export default function ProductDetails({ deal }: { deal: Deal }) {
  const partsRemaining = deal.partsTotal - deal.partsSold;
  const percent = Math.min(
    100,
    Math.round((deal.partsSold / deal.partsTotal) * 100)
  );
  const expiresIn = useMemo(() => {
    if (!deal.expiryDate) return null;
    const diff = new Date(deal.expiryDate).getTime() - Date.now();
    if (diff <= 0) return "expiré";
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    return `${days}j ${hours}h`;
  }, [deal.expiryDate]);

  return (
    <section className="mt-6 bg-white rounded-lg p-5 shadow-sm">
      <h2 className="text-2xl font-semibold">{deal.title}</h2>
      {deal.shortSubtitle && (
        <p className="text-sm text-muted-foreground mt-1">
          {deal.shortSubtitle}
        </p>
      )}

      <div className="flex items-center gap-4 mt-4">
        <Badge variant="secondary">{deal.rating} ★</Badge>
        <div className="text-sm text-gray-600">{deal.reviewsCount} avis</div>
        <div className="text-sm text-gray-500">{deal.location}</div>
        {deal.expiryDate && (
          <div className="ml-2 text-sm text-red-600">
            Expire dans {expiresIn}
          </div>
        )}
      </div>

      <Separator className="my-4" />

      <p className="mt-3 text-gray-700 leading-relaxed">{deal.description}</p>

      <div className="mt-4">
        <h4 className="font-medium">Disponibilité</h4>
        <div className="mt-2">
          <div className="w-full bg-gray-100 rounded-full h-3 overflow-hidden">
            <div
              className="h-3 bg-green-500"
              style={{ width: `${percent}%` }}
            />
          </div>
          <div className="mt-2 text-sm text-gray-700">
            {deal.partsSold} vendues • {partsRemaining} restantes (sur{" "}
            {deal.partsTotal})
          </div>
          <div className="mt-1 text-sm text-gray-600">
            Nécessaire pour activer l'offre : {deal.minRequired} parts
          </div>
        </div>
      </div>

      <Separator className="my-4" />

      <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <h3 className="font-semibold">Points forts</h3>
          <ul className="mt-2 list-disc list-inside text-gray-700">
            {deal.highlights?.map((h) => (
              <li key={h}>{h}</li>
            ))}
          </ul>
        </div>

        <div>
          <h3 className="font-semibold">Inclus</h3>
          <ul className="mt-2 list-disc list-inside text-gray-700">
            {deal.whatsIncluded?.map((w) => (
              <li key={w}>{w}</li>
            ))}
          </ul>
        </div>
      </div>

      <Separator className="my-4" />

      <div>
        <h4 className="font-medium">Fournisseur & packaging</h4>
        <div className="mt-2 text-sm text-gray-700">
          {deal.supplier?.name} • {deal.packaging?.method}
        </div>
      </div>

      <Separator className="my-4" />

      <div>
        <h4 className="font-medium">Conseils de cuisson</h4>
        <ul className="mt-2 list-disc list-inside text-gray-700">
          {deal.cookingTips?.map((t) => (
            <li key={t}>{t}</li>
          ))}
        </ul>
      </div>
    </section>
  );
}
