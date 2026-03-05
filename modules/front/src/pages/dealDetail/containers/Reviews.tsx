import { useMemo, useState } from "react";
import { Button } from "@/common/components/ui/button";
import { Avatar, AvatarFallback } from "@/common/components/ui/avatar";
import { Send } from "lucide-react";
import { timeAgo } from "@/common/utils/date";
import { HStack, StarRating, Textarea, VStack } from "@/common/components";
import { Heading } from "@/common/containers/Heading";
import { useAuth } from "@/common/context/AuthContext";
import {
  commentaireKeys,
  useCommentairesByDeal,
  useCreateCommentaire,
  useUsers,
} from "@/common/api";
import { useQueryClient } from "@tanstack/react-query";

// ==============================
// Types
// ==============================
type Review = {
  id: string;
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
  dealUuid,
  isMerchant = false,
}: {
  dealUuid: string;
  isMerchant?: boolean;
}) {
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const { data: commentaires = [], isLoading } =
    useCommentairesByDeal(dealUuid);
  const { data: users = [] } = useUsers();
  const createCommentaire = useCreateCommentaire({
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: commentaireKeys.byDeal(dealUuid),
      });
    },
  });

  const [newComment, setNewComment] = useState("");
  const [newRating, setNewRating] = useState(0);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const [replyOpenId, setReplyOpenId] = useState<string | null>(null);
  const [replyText, setReplyText] = useState("");

  const usersByUuid = useMemo(
    () =>
      new Map(
        users.map((u) => [
          u.uuid,
          `${u.prenom ?? ""} ${u.nom ?? ""}`.trim() || u.email,
        ]),
      ),
    [users],
  );

  const currentUserUuid = useMemo(() => {
    if (user?.id) return user.id;
    if (!user?.email) return null;
    return users.find((u) => u.email === user.email)?.uuid ?? null;
  }, [user?.email, user?.id, users]);

  const isAuthenticated = Boolean(currentUserUuid);

  const commentsByParent = useMemo(() => {
    const index = new Map<string, typeof commentaires>();
    commentaires.forEach((commentaire) => {
      const parent = commentaire.commentaireParentUuid;
      if (!parent) return;
      const list = index.get(parent) ?? [];
      list.push(commentaire);
      index.set(parent, list);
    });
    return index;
  }, [commentaires]);

  const reviews = useMemo<Review[]>(() => {
    return commentaires
      .filter((commentaire) => !commentaire.commentaireParentUuid)
      .sort(
        (a, b) =>
          new Date(b.dateCreation ?? 0).getTime() -
          new Date(a.dateCreation ?? 0).getTime(),
      )
      .map((commentaire) => {
        const replies = commentsByParent.get(commentaire.uuid ?? "") ?? [];
        const pertinent = replies.find((r) => r.estPertinent);
        const selectedReply = pertinent ?? replies[0];
        let reply: Review["reply"] = undefined;
        if (selectedReply) {
          reply = {
            author: "merchant",
            message: selectedReply.contenu,
            createdAt: selectedReply.dateCreation ?? new Date().toISOString(),
          };
        }

        return {
          id: commentaire.uuid ?? "",
          author:
            usersByUuid.get(commentaire.utilisateurUuid) ??
            `Utilisateur ${commentaire.utilisateurUuid.slice(0, 8)}`,
          rating: Number(commentaire.note) || 0,
          comment: commentaire.contenu,
          createdAt: commentaire.dateCreation ?? new Date().toISOString(),
          reply,
        };
      });
  }, [commentaires, commentsByParent, usersByUuid]);

  // ==============================
  // Handlers
  // ==============================
  const handleAddReview = async () => {
    if (!currentUserUuid || !newComment.trim() || newRating <= 0) return;
    setSubmitError(null);
    try {
      await createCommentaire.mutateAsync({
        contenu: newComment.trim(),
        note: Math.max(1, Math.min(5, Math.round(newRating))),
        utilisateurUuid: currentUserUuid,
        dealUuid,
        commentaireParentUuid: null,
        estPertinent: false,
      });
      setNewComment("");
      setNewRating(0);
    } catch {
      setSubmitError("Impossible de publier votre avis pour le moment.");
    }
  };

  const handleReply = async (review: Review) => {
    if (!currentUserUuid || !replyText.trim()) return;
    setSubmitError(null);
    try {
      await createCommentaire.mutateAsync({
        contenu: replyText.trim(),
        note: 5,
        utilisateurUuid: currentUserUuid,
        dealUuid,
        commentaireParentUuid: review.id,
        estPertinent: true,
      });
      setReplyText("");
      setReplyOpenId(null);
    } catch {
      setSubmitError("Impossible d'envoyer la reponse pour le moment.");
    }
  };

  return (
    <section className="mt-6 space-y-6">
      <Heading level={4} title="Avis des clients" />

      {!isMerchant && isAuthenticated && (
        <div className="border border-border rounded-lg p-4 bg-gray-50 dark:bg-gray-800 space-y-3">
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
              disabled={
                !currentUserUuid ||
                !newComment ||
                newRating === 0 ||
                createCommentaire.isPending
              }
              onClick={handleAddReview}
            >
              Publier
            </Button>
          </div>
          {submitError && <p className="text-xs text-red-600">{submitError}</p>}
        </div>
      )}

      {!isMerchant && !isAuthenticated && (
        <p className="text-sm text-muted-foreground">
          Connectez-vous pour laisser un avis.
        </p>
      )}

      {/* LISTE DES AVIS */}
      <VStack spacing={10}>
        {isLoading && (
          <p className="text-sm text-muted-foreground">
            Chargement des avis...
          </p>
        )}

        {!isLoading && reviews.length === 0 && (
          <p className="text-sm text-muted-foreground">
            Aucun avis pour le moment.
          </p>
        )}

        {reviews.map((review) => (
          <article
            key={review.id}
            className="border border-border rounded-lg p-4 bg-card space-y-3"
          >
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

            {isMerchant && (
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
                        disabled={
                          !replyText.trim() ||
                          !currentUserUuid ||
                          createCommentaire.isPending
                        }
                        onClick={() => handleReply(review)}
                      >
                        Envoyer
                      </Button>
                    </div>
                  </div>
                )}
              </>
            )}
          </article>
        ))}
      </VStack>
    </section>
  );
}
