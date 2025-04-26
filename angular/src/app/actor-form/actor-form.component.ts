import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { JsonPipe } from '@angular/common';

@Component({
	selector: 'app-actor-form',
	imports: [FormsModule, JsonPipe],
	templateUrl: './actor-form.component.html',
	styleUrl: './actor-form.component.css'
})
export class ActorFormComponent implements OnInit {

	skills = ['Acting', 'Singing', 'Dancing', 'Directing', 'Writing'];
	model = new Actor(1, 'John Doe', this.skills[0], 'Hollywood');
	submitted = false;


	ngOnInit(): void {
		const myActress = new Actor(1, 'John Doe', this.skills[0], 'Hollywood');
		console.log('My actress:', myActress);
	}
	

	onSubmit() {
		this.submitted = true;
	}

	newActor() {
		this.model = new Actor(1, '', '', 'Bollywood');
		this.submitted = false;
	}

	

}

export class Actor {
	constructor(
		public id: number,
		public name: string,
		public skill: string,
		public studio: string
	) { }
}