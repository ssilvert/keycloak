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
 import {Component, Input, OnInit} from '@angular/core';
 
 //export type View = "LargeCards" | "SmallCards" | "List";
 
 /**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2017 Red Hat Inc.
 */
 @Component({
    moduleId: module.id, // need this for styleUrls path to work properly with Systemjs
    selector: 'toolbar',
    templateUrl: 'toolbar.html',
    styleUrls: ['toolbar.css']
})
export class ToolbarComponent implements OnInit {
    @Input() selectableProps: string[];
    
    private isSortAscending: boolean = true;
    private sortBy: string = "";
    private filterBy: string = "";
    private filterText: string = "";
    
    public activeView: string = "LargeCards";
    
    ngOnInit() {
        if (this.selectableProps && this.selectableProps.length > 0) {
            this.sortBy = this.selectableProps[0];
            this.filterBy = this.selectableProps[0];
        }
    }
    
    private toggleSort() {
        this.isSortAscending = !this.isSortAscending;
    }
    
    private changeSortByProp(prop: string) {
        this.sortBy = prop;
    }
    
    private changeFilterByProp(prop: string) {
        this.filterBy = prop;
        this.filterText = "";
    }
    
    private capitalize(prop: string): string {
        if (!prop) return prop;
        
        const firstChar: string = prop.charAt(0).toUpperCase();
        if (prop.length === 1) return firstChar;
        
        return  firstChar + prop.substring(1);
    }
    
    private selectedFilterClass(prop: string): string {
        if (this.filterBy === prop) {
            return "selected";
        } else {
            return "";
        }
    }
    
    private selectedSortByClass(prop: string): string {
        if (this.sortBy === prop) {
            return "selected";
        } else {
            return "";
        }
    }
}


