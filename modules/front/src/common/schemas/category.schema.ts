import { z } from "zod";

export const categorySchema = z.object({
  nom: z.string().min(1, "Nom requis").max(50, "Nom trop long"),
  icone: z.string().optional(),
  description: z.string().optional(),
});
