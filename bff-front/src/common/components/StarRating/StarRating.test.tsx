import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import StarRating from "./index";

describe("StarRating", () => {
  it("affiche le bon nombre d'étoiles", () => {
    render(<StarRating value={3} max={5} />);
    
    const stars = screen.getAllByRole("button");
    expect(stars).toHaveLength(5);
  });

  it("affiche la valeur numérique quand showValue est true", () => {
    render(<StarRating value={3.5} showValue />);
    
    expect(screen.getByText("3.5 / 5")).toBeInTheDocument();
  });

  it("n'affiche pas la valeur numérique par défaut", () => {
    render(<StarRating value={3} />);
    
    expect(screen.queryByText(/\//)).not.toBeInTheDocument();
  });

  it("les boutons sont désactivés en mode readOnly", () => {
    render(<StarRating value={3} readOnly />);
    
    const stars = screen.getAllByRole("button");
    stars.forEach((star) => {
      expect(star).toBeDisabled();
    });
  });

  it("appelle onChange quand on clique sur une étoile (mode interactif)", () => {
    const onChange = vi.fn();
    render(<StarRating value={2} readOnly={false} onChange={onChange} />);
    
    const stars = screen.getAllByRole("button");
    fireEvent.click(stars[3]); // 4ème étoile
    
    expect(onChange).toHaveBeenCalledWith(4);
  });

  it("n'appelle pas onChange en mode readOnly", () => {
    const onChange = vi.fn();
    render(<StarRating value={2} readOnly onChange={onChange} />);
    
    const stars = screen.getAllByRole("button");
    fireEvent.click(stars[3]);
    
    expect(onChange).not.toHaveBeenCalled();
  });

  it("a les bons labels d'accessibilité", () => {
    render(<StarRating value={3} max={5} />);
    
    expect(screen.getByLabelText("Rate 1 star")).toBeInTheDocument();
    expect(screen.getByLabelText("Rate 5 star")).toBeInTheDocument();
  });

  it("affiche le nombre d'étoiles personnalisé", () => {
    render(<StarRating value={5} max={10} />);
    
    const stars = screen.getAllByRole("button");
    expect(stars).toHaveLength(10);
  });
});
