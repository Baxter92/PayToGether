import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import CheckoutStep from "./CheckoutStep";

describe("CheckoutStep", () => {
  const defaultProps = {
    stepNumber: 1,
    title: "Étape 1",
    description: "Description de l'étape",
    isActive: false,
    isCompleted: false,
  };

  it("affiche le numéro de l'étape", () => {
    render(<CheckoutStep {...defaultProps} />);
    
    expect(screen.getByText("1")).toBeInTheDocument();
  });

  it("affiche le titre de l'étape", () => {
    render(<CheckoutStep {...defaultProps} />);
    
    expect(screen.getByText("Étape 1")).toBeInTheDocument();
  });

  it("affiche la description de l'étape", () => {
    render(<CheckoutStep {...defaultProps} />);
    
    expect(screen.getByText("Description de l'étape")).toBeInTheDocument();
  });

  it("affiche une coche quand l'étape est complétée", () => {
    const { container } = render(<CheckoutStep {...defaultProps} isCompleted />);
    
    // Le numéro ne devrait plus être visible
    expect(screen.queryByText("1")).not.toBeInTheDocument();
    // L'icône check devrait être présente
    expect(container.querySelector("svg")).toBeInTheDocument();
  });

  it("affiche les enfants quand l'étape est active", () => {
    render(
      <CheckoutStep {...defaultProps} isActive>
        <div>Contenu de l'étape</div>
      </CheckoutStep>
    );
    
    expect(screen.getByText("Contenu de l'étape")).toBeInTheDocument();
  });

  it("n'affiche pas les enfants quand l'étape n'est pas active", () => {
    render(
      <CheckoutStep {...defaultProps} isActive={false}>
        <div>Contenu de l'étape</div>
      </CheckoutStep>
    );
    
    expect(screen.queryByText("Contenu de l'étape")).not.toBeInTheDocument();
  });

  it("applique le style complété au cercle", () => {
    const { container } = render(<CheckoutStep {...defaultProps} isCompleted />);
    
    const circle = container.querySelector(".bg-primary");
    expect(circle).toBeInTheDocument();
  });

  it("applique le style actif au cercle", () => {
    const { container } = render(<CheckoutStep {...defaultProps} isActive />);
    
    const circle = container.querySelector(".border-primary");
    expect(circle).toBeInTheDocument();
  });
});
