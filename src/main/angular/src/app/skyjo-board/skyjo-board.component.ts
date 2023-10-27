import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SkyjoService} from "../../services/skyjo.service";

@Component({
  selector: 'skyjo-board',
  templateUrl: './skyjo-board.component.html',
  styleUrls: ['./skyjo-board.component.scss']
})
export class SkyjoBoardComponent {
  @Input() cards: number[] = [];
  @Input() clickableCards?: boolean[] = [];

  @Output() onCardClicked: EventEmitter<number> = new EventEmitter<number>();

  isClickAllowed(cardIndex: number) {
    return !!this.clickableCards && this.clickableCards[cardIndex];
  }

  isClickDisallowed(cardIndex: number) {
    return !!this.clickableCards && !this.clickableCards[cardIndex];
  }

  isCardDeleted(cardIndex: number): boolean {
    if (this.cards.length > cardIndex)
      return SkyjoService.getCardStatus(this.cards[cardIndex]) === 'deleted';
    return false;
  }

  getCardValue(cardIndex: number): string {
    if (this.cards.length <= cardIndex)
      return '?';

    switch (SkyjoService.getCardStatus(this.cards[cardIndex])) {
      case 'deleted': return '';
      case 'hidden': return '?';
      case 'shown': return this.cards[cardIndex].toString();

      default: return '-';
    }
  }
}
