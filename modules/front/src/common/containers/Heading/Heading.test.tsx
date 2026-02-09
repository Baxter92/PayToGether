import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { Heading } from "./index";

describe("Heading", () => {
  it("affiche le titre correctement", () => {
    render(<Heading title="Mon titre" />);

    expect(screen.getByRole("heading")).toHaveTextContent("Mon titre");
  });

  it("affiche la description quand elle est fournie", () => {
    render(<Heading title="Titre" description="Ma description" />);

    expect(screen.getByText("Ma description")).toBeInTheDocument();
  });

  it("n'affiche pas de description si non fournie", () => {
    render(<Heading title="Titre" />);

    const paragraphs = screen.queryAllByRole("paragraph");
    expect(paragraphs).toHaveLength(0);
  });

  it("utilise le bon niveau de heading (h1-h6)", () => {
    const { rerender } = render(<Heading title="Titre" level={1} />);
    expect(screen.getByRole("heading", { level: 1 })).toBeInTheDocument();

    rerender(<Heading title="Titre" level={3} />);
    expect(screen.getByRole("heading", { level: 3 })).toBeInTheDocument();

    rerender(<Heading title="Titre" level={6} />);
    expect(screen.getByRole("heading", { level: 6 })).toBeInTheDocument();
  });

  it("affiche les actions quand fournies", () => {
    render(
      <Heading
        title="Titre"
        actions={<button>Action</button>}
      />
    );

    expect(screen.getByRole("button", { name: "Action" })).toBeInTheDocument();
  });

  it("applique le style underline quand underline est true", () => {
    const { container } = render(
      <Heading title="Titre" underline underlineStyle="line" />
    );

    const underlineDiv = container.querySelector(".border-b-2");
    expect(underlineDiv).toBeInTheDocument();
  });

  it("applique le style text underline correctement", () => {
    render(<Heading title="Titre" underline underlineStyle="text" />);

    const heading = screen.getByRole("heading");
    expect(heading).toHaveClass("underline");
  });

  it("centre le texte quand align=center", () => {
    render(<Heading title="Titre" align="center" />);

    const heading = screen.getByRole("heading");
    expect(heading).toHaveClass("text-center");
  });

  it("aligne à droite quand align=right", () => {
    render(<Heading title="Titre" align="right" />);

    const heading = screen.getByRole("heading");
    expect(heading).toHaveClass("text-right");
  });
});
