import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import HelpSection from "./HelpSection";
import { BrowserRouter } from "react-router-dom";

describe("HelpSection", () => {
  const defaultProps = {
    onBack: vi.fn(),
    onHome: vi.fn(),
  };

  const renderWithRouter = (component: React.ReactNode) => {
    return render(<BrowserRouter>{component}</BrowserRouter>);
  };

  it("affiche le titre", () => {
    renderWithRouter(<HelpSection {...defaultProps} />);

    expect(screen.getByText("Besoin d'aide ?")).toBeInTheDocument();
  });

  it("affiche le message de support", () => {
    renderWithRouter(<HelpSection {...defaultProps} />);

    expect(screen.getByText(/équipe de support/)).toBeInTheDocument();
  });

  it("affiche le bouton Retour", () => {
    renderWithRouter(<HelpSection {...defaultProps} />);

    expect(screen.getByRole("button", { name: "Retour" })).toBeInTheDocument();
  });

  it("affiche le bouton Accueil", () => {
    renderWithRouter(<HelpSection {...defaultProps} />);

    expect(screen.getByRole("button", { name: "Accueil" })).toBeInTheDocument();
  });

  it("appelle onBack quand on clique sur Retour", () => {
    const onBack = vi.fn();
    renderWithRouter(<HelpSection {...defaultProps} onBack={onBack} />);

    fireEvent.click(screen.getByRole("button", { name: "Retour" }));
    expect(onBack).toHaveBeenCalledTimes(1);
  });

  it("appelle onHome quand on clique sur Accueil", () => {
    const onHome = vi.fn();
    renderWithRouter(<HelpSection {...defaultProps} onHome={onHome} />);

    fireEvent.click(screen.getByRole("button", { name: "Accueil" }));
    expect(onHome).toHaveBeenCalledTimes(1);
  });
});
