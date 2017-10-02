/*
 * Copyright 2017 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import {Component, OnInit} from '@angular/core';
import {Response} from '@angular/http';

import {AccountServiceClient} from '../../account-service/account.service';

import {PropertyLabel} from '../widgets/property.label';
import {ActionButton} from '../widgets/action.button';
import {RefreshButton, Refreshable} from '../widgets/refresh.button';

import {Event} from './event';

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2017 Red Hat Inc.
 */
@Component({
    selector: 'events-account-page',
    templateUrl: './events-page.component.html',
    styleUrls: ['./events-page.component.css']
})
export class EventsPageComponent implements Refreshable {
    private filterLabels: PropertyLabel[] = [];
    private sortLabels: PropertyLabel[] = [];

    private events: Event[] = [];
    
    private actionButtons: ActionButton[] = [];
    
    constructor(private accountSvc: AccountServiceClient ) {
        this.initPropLabels();
        this.actionButtons.push(new RefreshButton(accountSvc,"/sessions", this));
        accountSvc.doGetRequest("/events", (res: Response) => this.refresh(res));
    }
    
    private initPropLabels(): void {
        //TODO: localize the labels
        this.filterLabels.push({prop: "type", label: "Type"});
        this.filterLabels.push({prop: "clientId", label: "Application"});
        
        this.sortLabels.push({prop: "time", label: "Time/Date"});
        this.sortLabels.push({prop: "clientId", label: "Application"});
    }
    
    public refresh(res: Response) {
      console.log('**** response from events REST API ***');
      console.log(JSON.stringify(res));
      console.log('***************************************');
      
      const newEvents: Event[] = [];
      for (let event of res.json()) {
          newEvents.push(new Event(event));
      }
      
      // reference must change to trigger pipes
      this.events = newEvents;
    }
}


