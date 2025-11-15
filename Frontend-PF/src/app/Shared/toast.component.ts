import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from './toast.service';
import { Subscription, timer } from 'rxjs';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.html',
  styleUrls: ['./toast.scss']
})
export class ToastComponent implements OnDestroy {
  messages: ToastMessage[] = [];
  sub: Subscription;

  constructor(private toast: ToastService) {
    this.sub = this.toast.messages$.subscribe(msg => {
      this.messages.push(msg);
      // auto remove after 4s
      timer(4000).subscribe(() => this.remove(msg.id!));
    });
  }

  remove(id: string) {
    this.messages = this.messages.filter(m => m.id !== id);
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
