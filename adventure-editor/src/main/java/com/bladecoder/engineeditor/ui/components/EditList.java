/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.bladecoder.engineeditor.ui.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public abstract class EditList<T> extends Table {
	
	protected EditToolbar toolbar;
    protected CustomList<T> list;
    protected Skin skin;
    protected Container<ScrollPane> container;
	
	public EditList(Skin skin) {
		super(skin);
		
		this.skin = skin;
		
		
		list = new CustomList<T>(skin);
		
		Array<T> items = new Array<T>();
		list.setItems(items);
			
		ScrollPane scrollPane = new ScrollPane(list, skin);
		container = new Container<ScrollPane>(scrollPane);
		container.fill();
		container.prefHeight(1000);
		
		toolbar = new EditToolbar(skin);
//		debug();
		add(toolbar).expandX().fillX();
		row();
		add(container).expand().fill();
		
		toolbar.addCreateListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				create();
			}
		});
		
		toolbar.addEditListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				edit();
			}
		});
		
		toolbar.addDeleteListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				delete();
			}
		});
		
		toolbar.addCopyListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				copy();
			}
		});
		
		toolbar.addPasteListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				paste();
			}
		});
		
		list.addListener(new InputListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer,  com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
//				EditorLogger.debug("ENTER - X: " + x + " Y: " + y);
				getStage().setScrollFocus(list);
			}
		});
    }
	
	
	
	public void setCellRenderer(CellRenderer<T> r) {
		list.setCellRenderer(r);	
	}
	
	public Array<T> getItems () {
		return list.getItems();
	}
	
	public void addItem(T item) {		
		list.getItems().add(item);
	}
	
	public void clear() {
		list.getItems().clear();
	}
	
	abstract protected void create();
	abstract protected void edit();
	abstract protected void delete();
	abstract protected void copy();
	abstract protected void paste();
}
