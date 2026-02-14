import { z } from "zod";
import { StatutDeal } from "../api";

export const dealSchema = z.object({
  title: z.string().min(3, "Titre trop court").max(100),
  shortSubtitle: z.string().optional(),
  description: z.string().min(10, "Description trop courte"),

  price: z.number().min(0, "Le prix doit etre positif"),
  originalPrice: z.number().optional(),
  currency: z.enum(["USD"]),

  partsTotal: z.number().min(1, "Le nombre de parts est requis"),
  minRequired: z.number().min(1, "Le minimum requis doit etre superieur a 0"),
  expiryDate: z.date().optional(),

  location: z.string().min(3, "Le lieu est requis"),
  categoryId: z.string().min(1, "La categorie est requise"),

  highlights: z.string().optional(),
  whatsIncluded: z.string().optional(),

  status: z.enum(Object.entries(StatutDeal).map(([key, value]) => value)),

  supplierName: z.string().optional(),
  packagingMethod: z.string().optional(),
  merchantId: z.string().min(1, "Le fournisseur est requis"),
  images: z
    .array(z.instanceof(File))
    .min(1, "Au moins une image est requise")
    .max(5, "Maximum 5 images"),
});
