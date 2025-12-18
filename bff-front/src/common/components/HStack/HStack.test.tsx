import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import HStack from "./index";

describe("HStack", () => {
  it("rend les enfants correctement", () => {
    render(
      <HStack>
        <span>Item 1</span>
        <span>Item 2</span>
      </HStack>
    );
    
    expect(screen.getByText("Item 1")).toBeInTheDocument();
    expect(screen.getByText("Item 2")).toBeInTheDocument();
  });

  it("applique flex par défaut", () => {
    const { container } = render(<HStack>Content</HStack>);
    
    expect(container.firstChild).toHaveClass("flex");
  });

  it("applique le spacing personnalisé", () => {
    const { container } = render(<HStack spacing={16}>Content</HStack>);
    
    expect(container.firstChild).toHaveStyle({ gap: "16px" });
  });

  it("applique items-center par défaut", () => {
    const { container } = render(<HStack>Content</HStack>);
    
    expect(container.firstChild).toHaveClass("items-center");
  });

  it("applique align=start correctement", () => {
    const { container } = render(<HStack align="start">Content</HStack>);
    
    expect(container.firstChild).toHaveClass("items-start");
  });

  it("applique align=end correctement", () => {
    const { container } = render(<HStack align="end">Content</HStack>);
    
    expect(container.firstChild).toHaveClass("items-end");
  });

  it("applique justify=center correctement", () => {
    const { container } = render(<HStack justify="center">Content</HStack>);
    
    expect(container.firstChild).toHaveClass("justify-center");
  });

  it("applique justify=between correctement", () => {
    const { container } = render(<HStack justify="between">Content</HStack>);
    
    expect(container.firstChild).toHaveClass("justify-between");
  });

  it("applique flex-wrap quand wrap=true", () => {
    const { container } = render(<HStack wrap>Content</HStack>);
    
    expect(container.firstChild).toHaveClass("flex-wrap");
  });

  it("n'applique pas flex-wrap par défaut", () => {
    const { container } = render(<HStack>Content</HStack>);
    
    expect(container.firstChild).not.toHaveClass("flex-wrap");
  });

  it("passe les className personnalisées", () => {
    const { container } = render(<HStack className="custom-class">Content</HStack>);
    
    expect(container.firstChild).toHaveClass("custom-class");
  });
});
