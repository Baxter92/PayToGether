import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import HelpSection from "./HelpSection";

describe("HelpSection", () => {
  const defaultProps = {
    onBack: vi.fn(),
    onHome: vi.fn(),
  };

  it("affiche le titre", () => {
    render(<HelpSection {...defaultProps} />);
    
    expect(screen.getByText("Besoin d'aide ?")).toBeInTheDocument();
  });

  it("affiche le message de support", () => {
    render(<HelpSection {...defaultProps} />);
    
    expect(screen.getByText(/équipe de support/)).toBeInTheDocument();
  });

  it("affiche le bouton Retour", () => {
    render(<HelpSection {...defaultProps} />);
    
    expect(screen.getByRole("button", { name: "Retour" })).toBeInTheDocument();
  });

  it("affiche le bouton Accueil", () => {
    render(<HelpSection {...defaultProps} />);
    
    expect(screen.getByRole("button", { name: "Accueil" })).toBeInTheDocument();
  });

  it("appelle onBack quand on clique sur Retour", () => {
    const onBack = vi.fn();
    render(<HelpSection {...defaultProps} onBack={onBack} />);
    
    fireEvent.click(screen.getByRole("button", { name: "Retour" }));
    expect(onBack).toHaveBeenCalledTimes(1);
  });

  it("appelle onHome quand on clique sur Accueil", () => {
    const onHome = vi.fn();
    render(<HelpSection {...defaultProps} onHome={onHome} />);
    
    fireEvent.click(screen.getByRole("button", { name: "Accueil" }));
    expect(onHome).toHaveBeenCalledTimes(1);
  });
});
