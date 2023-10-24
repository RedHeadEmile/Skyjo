import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-skyjo-board',
  templateUrl: './skyjo-board.component.html',
  styleUrls: ['./skyjo-board.component.scss']
})
export class SkyjoBoardComponent {
  @Input() cards: number[] = [];
  @Input() canCardBeClicked: boolean = true;

  @Output() onCardClicked: EventEmitter<number> = new EventEmitter<number>();

  getCardValue(cardIndex: number) {
    if (this.cards.length > cardIndex) {
      const value = this.cards[cardIndex];
      if (value < -2 || value > 12)
        return '?';
      return value;
    }
    return '?';
  }
}
