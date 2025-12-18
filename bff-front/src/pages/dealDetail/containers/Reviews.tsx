import { useState } from "react";
import { Button } from "@/common/components/ui/button";
import { Avatar, AvatarFallback } from "@/common/components/ui/avatar";
import { ChevronRight, Send } from "lucide-react";
import { timeAgo } from "@/common/utils/date";
import { HStack, StarRating, Textarea, VStack } from "@/common/components";
import { Heading } from "@/common/containers/Heading";

// ==============================
// Types
// ==============================
type Review = {
  id: number;
  author: string;
  rating: number;
  comment: string;
  createdAt: string;
  reply?: {
    author: "merchant";
    message: string;
    createdAt: string;
  };
};

// ==============================
// Component
// ==============================
export default function Reviews({
  count,
  isMerchant = false,
}: {
  count: number;
  isMerchant?: boolean;
}) {
  const initialReviews: Review[] = [1, 2, 3]
    .slice(0, Math.min(3, count))
    .map((i) => ({
      id: i,
      author: `Utilisateur ${i}`,
      rating: 4 + i * 0.2,
      comment: "Très bon produit, viande de qualité et livraison soignée.",
      createdAt: new Date(Date.now() - i * 86400000).toISOString(),
      reply:
        i === 1
          ? {
              author: "merchant",
              message: "Merci pour votre retour, au plaisir de vous revoir !",
              createdAt: new Date(Date.now() - 3600000).toISOString(),
            }
          : undefined,
    }));

  const [reviews, setReviews] = useState<Review[]>(initialReviews);

  const [newComment, setNewComment] = useState("");
  const [newRating, setNewRating] = useState(0);

  const [replyOpenId, setReplyOpenId] = useState<number | null>(null);
  const [replyText, setReplyText] = useState("");

  // ==============================
  // Handlers
  // ==============================
  const handleAddReview = () => {
    setReviews((prev) => [
      {
        id: Date.now(),
        author: "Vous",
        rating: newRating,
        comment: newComment,
        createdAt: new Date().toISOString(),
      },
      ...prev,
    ]);
    setNewComment("");
    setNewRating(0);
  };

  const handleReply = (reviewId: number) => {
    setReviews((prev) =>
      prev.map((r) =>
        r.id === reviewId
          ? {
              ...r,
              reply: {
                author: "merchant",
                message: replyText,
                createdAt: new Date().toISOString(),
              },
            }
          : r
      )
    );
    setReplyText("");
    setReplyOpenId(null);
  };

  return (
    <section className="mt-6 space-y-6">
      <Heading level={4} title="Avis des clients" />

      {!isMerchant && (
        <div className="border rounded-lg p-4 bg-gray-50 space-y-3">
          <Heading level={6} title="Ajouter un avis" />

          <StarRating
            value={newRating}
            onChange={setNewRating}
            size="md"
            readOnly={false}
          />

          <Textarea
            placeholder="Écrivez votre avis..."
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
          />

          <div className="flex justify-end">
            <Button
              size="sm"
              leftIcon={<Send className="w-4 h-4" />}
              disabled={!newComment || newRating === 0}
              onClick={handleAddReview}
            >
              Publier
            </Button>
          </div>
        </div>
      )}

      {/* LISTE DES AVIS */}
      <VStack spacing={10}>
        {reviews.map((review) => (
          <article key={review.id} className="border rounded-lg p-4 space-y-3">
            {/* Avis client */}
            <HStack spacing={10} align="start">
              <Avatar className="rounded-lg">
                <AvatarFallback>{review.author[0]}</AvatarFallback>
              </Avatar>

              <div className="flex-1">
                <HStack justify="between">
                  <Heading level={6} title={review.author} />
                  <HStack align="center" spacing={4}>
                    <StarRating value={review.rating} size="sm" />
                    <span className="text-xs text-muted-foreground">
                      {timeAgo(review.createdAt)}
                    </span>
                  </HStack>
                </HStack>

                <p className="mt-1 text-gray-700">{review.comment}</p>
              </div>
            </HStack>

            {/* 💬 RÉPONSE EXISTANTE */}
            {review.reply && (
              <div className="ml-10 border-l pl-4 text-sm space-y-1">
                <div className="font-medium text-gray-800">
                  Réponse du marchand
                </div>
                <div className="text-gray-700">{review.reply.message}</div>
                <div className="text-xs text-muted-foreground">
                  {timeAgo(review.reply.createdAt)}
                </div>
              </div>
            )}

            <>
              <Button
                size="sm"
                variant="ghost"
                onClick={() =>
                  setReplyOpenId(replyOpenId === review.id ? null : review.id)
                }
              >
                Répondre
              </Button>

              {replyOpenId === review.id && (
                <div className="mt-2 space-y-2">
                  <Textarea
                    placeholder="Votre réponse..."
                    value={replyText}
                    onChange={(e) => setReplyText(e.target.value)}
                  />
                  <div className="flex justify-end gap-2">
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() => setReplyOpenId(null)}
                    >
                      Annuler
                    </Button>
                    <Button
                      size="sm"
                      leftIcon={<Send className="w-4 h-4" />}
                      disabled={!replyText.trim()}
                      onClick={() => handleReply(review.id)}
                    >
                      Envoyer
                    </Button>
                  </div>
                </div>
              )}
            </>
          </article>
        ))}

        <Button variant="ghost" leftIcon={<ChevronRight className="w-4 h-4" />}>
          Voir tous les avis
        </Button>
      </VStack>
    </section>
  );
}
