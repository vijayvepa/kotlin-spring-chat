import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { JsonPipe } from '@angular/common';
import { ActorFormComponent } from './actor-form/actor-form.component';

@Component({
	selector: 'app-root',
	imports: [FormsModule, JsonPipe, ActorFormComponent],
	templateUrl: './app.component.html',
	styleUrl: './app.component.css'
})
export class AppComponent{

	title = 'angular';
	messages: string[] = ["Nothing", "at", "all"]
	newMessage: string | undefined = undefined ;

	addMessage() {
		this.messages.push(this.newMessage!);
	}
	
	clearMessages() {
		this.messages = [];
	}
}

