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
 
 /**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2017 Red Hat Inc.
 */
export class Event {

    constructor(private event: any) {}
    
    get time(): number {
        return this.event.time;
    }
    
    get type(): string {
        return this.event.type;
    }
    
    get realmId(): string {
        return this.event.realmId;
    }
    
    get clientId(): string {
        return this.event.clientId;
    }
    
    get userId(): string {
        return this.event.userId;
    }
    
    get sessionId(): string {
        return this.event.sessionId;
    }
    
    get error(): string {
        return this.event.error;
    }
    
    get ipAddress(): string {
        return this.event.ipAddress;
    }
    
    get details(): any {
        return 
    }
}