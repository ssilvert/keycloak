/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {Component, OnInit} from '@angular/core';
import {KeycloakService} from '../keycloak-service/keycloak.service';
import {ORIGINAL_INCOMING_URL} from '../app.module';

declare const resourceUrl: string;
declare const referrer: string;
declare const referrer_uri: string;

@Component({
    selector: 'app-top-nav',
    templateUrl: './top-nav.component.html',
    styleUrls: ['./top-nav.component.css']
})
export class TopNavComponent implements OnInit {

    public resourceUrl: string = resourceUrl;
    
    public referrer: string;
    public referrerUri: string;

    constructor(private keycloakService: KeycloakService) {
        if (typeof referrer !== "undefined") {
            this.referrer = referrer;
            this.referrerUri = referrer_uri;
        }
    }

    ngOnInit() {
    }

    logout() {
        this.keycloakService.logout();
    }

}
