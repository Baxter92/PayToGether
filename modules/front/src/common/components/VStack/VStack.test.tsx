import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import VStack from "./index";

describe("VStack", () => {
  it("rend les enfants correctement", () => {
    render(
      <VStack>
        <span>Item 1</span>
        <span>Item 2</span>
      </VStack>
    );

    expect(screen.getByText("Item 1")).toBeInTheDocument();
    expect(screen.getByText("Item 2")).toBeInTheDocument();
  });

  it("applique flex flex-col par défaut", () => {
    const { container } = render(<VStack>Content</VStack>);

    expect(container.firstChild).toHaveClass("flex", "flex-col");
  });

  it("applique le spacing par défaut (16px)", () => {
    const { container } = render(<VStack>Content</VStack>);

    expect(container.firstChild).toHaveStyle({ gap: "16px" });
  });

  it("applique le spacing personnalisé", () => {
    const { container } = render(<VStack spacing={24}>Content</VStack>);

    expect(container.firstChild).toHaveStyle({ gap: "24px" });
  });

  it("applique items-stretch par défaut", () => {
    const { container } = render(<VStack>Content</VStack>);

    expect(container.firstChild).toHaveClass("items-stretch");
  });

  it("applique align=center correctement", () => {
    const { container } = render(<VStack align="center">Content</VStack>);

    expect(container.firstChild).toHaveClass("items-center");
  });

  it("applique justify=center correctement", () => {
    const { container } = render(<VStack justify="center">Content</VStack>);

    expect(container.firstChild).toHaveClass("justify-center");
  });

  it("applique justify=between correctement", () => {
    const { container } = render(<VStack justify="between">Content</VStack>);

    expect(container.firstChild).toHaveClass("justify-between");
  });

  it("applique w-full par défaut", () => {
    const { container } = render(<VStack>Content</VStack>);

    expect(container.firstChild).toHaveClass("w-full");
  });

  it("applique flex-wrap quand wrap=true", () => {
    const { container } = render(<VStack wrap>Content</VStack>);

    expect(container.firstChild).toHaveClass("flex-wrap");
  });

  it("passe les className personnalisées", () => {
    const { container } = render(<VStack className="custom-class">Content</VStack>);

    expect(container.firstChild).toHaveClass("custom-class");
  });
});
