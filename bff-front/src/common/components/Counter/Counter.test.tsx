import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Counter from "./index";

const renderWithRouter = (component: React.ReactNode) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe("Counter", () => {
  const defaultProps = {
    qty: 5,
    setQty: vi.fn(),
    min: 1,
    max: 10,
  };

  it("affiche la quantité actuelle", () => {
    renderWithRouter(<Counter {...defaultProps} />);
    expect(screen.getByText("5")).toBeInTheDocument();
  });

  it("incrémente la quantité quand on clique sur +", () => {
    const setQty = vi.fn();
    renderWithRouter(<Counter {...defaultProps} setQty={setQty} />);

    fireEvent.click(screen.getByRole("button-add"));
    expect(setQty).toHaveBeenCalledWith(6);
  });

  it("décrémente la quantité quand on clique sur -", () => {
    const setQty = vi.fn();
    renderWithRouter(<Counter {...defaultProps} setQty={setQty} />);

    fireEvent.click(screen.getByRole("button-minus"));
    expect(setQty).toHaveBeenCalledWith(4);
  });

  it("désactive le bouton - quand qty = min", () => {
    renderWithRouter(<Counter {...defaultProps} qty={1} />);

    const minusButton = screen.getByRole("button-minus");
    expect(minusButton).toBeDisabled();
  });

  it("désactive le bouton + quand qty = max", () => {
    renderWithRouter(<Counter {...defaultProps} qty={10} />);

    const plusButton = screen.getByRole("button-add");
    expect(plusButton).toBeDisabled();
  });

  it("ne dépasse pas le max", () => {
    const setQty = vi.fn();
    renderWithRouter(<Counter {...defaultProps} qty={10} setQty={setQty} />);

    fireEvent.click(screen.getByRole("button-add"));
    expect(setQty).not.toHaveBeenCalled();
  });

  it("ne descend pas en dessous du min", () => {
    const setQty = vi.fn();
    renderWithRouter(<Counter {...defaultProps} qty={1} setQty={setQty} />);

    fireEvent.click(screen.getByRole("button-minus"));
    expect(setQty).not.toHaveBeenCalled();
  });
});
