import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import DealCard from "./index";

const mockDeal = {
  id: 1,
  title: "Massage relaxant 1h",
  image: "/test-image.jpg",
  originalPrice: 100,
  groupPrice: 50,
  unit: "personne",
  sold: 35,
  total: 50,
  deadline: "2024-12-31",
  discount: 50,
};

const renderWithRouter = (component: React.ReactNode) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe("DealCard", () => {
  it("affiche le titre du deal", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(screen.getByText("Massage relaxant 1h")).toBeInTheDocument();
  });

  it("affiche le prix de groupe", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(screen.getByText("50€")).toBeInTheDocument();
  });

  it("affiche le prix original barré", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(screen.getByText("100€")).toBeInTheDocument();
  });

  it("affiche le pourcentage de réduction", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(screen.getByText("-50%")).toBeInTheDocument();
  });

  it("affiche le nombre de ventes", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(screen.getByText("35/50 vendus")).toBeInTheDocument();
  });

  it("affiche la deadline", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(screen.getByText("2024-12-31")).toBeInTheDocument();
  });

  it("affiche l'unité de prix", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(screen.getByText("par personne")).toBeInTheDocument();
  });

  it("affiche le badge Populaire quand plus de 70% vendus", () => {
    const hotDeal = {
      ...mockDeal,
      sold: 36, // 36 / 50 = 72%
    };
    renderWithRouter(<DealCard deal={hotDeal} />);

    expect(screen.getByText("Populaire")).toBeInTheDocument();
  });

  it("n'affiche pas le badge Populaire quand moins de 70% vendus", () => {
    const lowSalesDeal = { ...mockDeal, sold: 10 };
    renderWithRouter(<DealCard deal={lowSalesDeal} />);

    expect(screen.queryByText("Populaire")).not.toBeInTheDocument();
  });

  it("a un lien vers la page de détail", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", "/deals/1");
  });

  it("affiche l'image du deal", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    const image = screen.getByAltText("Massage relaxant 1h");
    expect(image).toHaveAttribute("src", "/test-image.jpg");
  });

  it("affiche le bouton Voir le deal", () => {
    renderWithRouter(<DealCard deal={mockDeal} />);

    expect(
      screen.getByRole("button", { name: "Voir le deal" })
    ).toBeInTheDocument();
  });
});
