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
package com.bladecoder.engine.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.actions.ActionCallback;
import com.bladecoder.engine.anim.AnimationDesc;
import com.bladecoder.engine.anim.SpritePosTween;
import com.bladecoder.engine.anim.SpriteScaleTween;
import com.bladecoder.engine.anim.Tween;
import com.bladecoder.engine.anim.WalkTween;
import com.bladecoder.engine.assets.EngineAssetManager;
import com.bladecoder.engine.loader.SerializationHelper;
import com.bladecoder.engine.loader.SerializationHelper.Mode;
import com.bladecoder.engine.util.InterpolationMode;

public class SpriteActor extends InteractiveActor {

	public static enum DepthType {
		NONE, VECTOR
	};

	protected ActorRenderer renderer;
	protected SpritePosTween posTween;
	private SpriteScaleTween scaleTween;
	private float scale = 1.0f;

	/** Scale sprite acording to the scene depth map */
	private DepthType depthType = DepthType.NONE;

	private boolean bboxFromRenderer = false;

	public void setRenderer(ActorRenderer r) {
		renderer = r;
	}

	public ActorRenderer getRenderer() {
		return renderer;
	}

	public DepthType getDepthType() {
		return depthType;
	}

	public void setDepthType(DepthType v) {
		depthType = v;
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);

		if (scene != null) {
			if (depthType == DepthType.VECTOR) {
				// interpolation equation
				float s = scene.getFakeDepthScale(y);

				setScale(s);
			}
		}

	}

	public boolean isBboxFromRenderer() {
		return bboxFromRenderer;
	}

	public void setBboxFromRenderer(boolean v) {
		this.bboxFromRenderer = v;

		if (v)
			renderer.updateBboxFromRenderer(bbox);
	}

	public float getWidth() {
		return renderer.getWidth() * scale;
	}

	public float getHeight() {
		return renderer.getHeight() * scale;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
		bbox.setScale(scale, scale);
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		if (visible) {
			renderer.update(delta);

			if (posTween != null) {
				if (posTween.isComplete()) {
					posTween = null;
				} else {
					posTween.update(this, delta);
				}
			}

			if (scaleTween != null) {
				scaleTween.update(this, delta);
				if (scaleTween.isComplete()) {
					scaleTween = null;
				}
			}
		}
	}

	public void draw(SpriteBatch batch) {
		if (isVisible()) {
			if (scale != 0)
				renderer.draw(batch, getX(), getY(), scale);
		}
	}

	public void startAnimation(String id, ActionCallback cb) {
		startAnimation(id, Tween.Type.SPRITE_DEFINED, 1, cb);
	}

	public void startAnimation(String id, Tween.Type repeatType, int count, ActionCallback cb) {

		inAnim();

		// resets posTween when walking
		if (posTween != null && posTween instanceof WalkTween)
			posTween = null;

		renderer.startAnimation(id, repeatType, count, cb);

		outAnim();
	}

	/**
	 * Actions to do when setting an animation: - stop previous animation sound
	 * - add 'out' distance from previous animation
	 */
	protected void inAnim() {
		AnimationDesc fa = renderer.getCurrentAnimation();

		if (fa != null) {

			if (fa.sound != null) {
				stopSound(fa.sound);
			}

			Vector2 outD = fa.outD;

			if (outD != null) {
				float s = EngineAssetManager.getInstance().getScale();

				setPosition(getX() + outD.x * s, getY() + outD.y * s);
			}
		}
	}

	/**
	 * Actions to do when setting an animation: - play animation sound - add
	 * 'in' distance
	 */
	protected void outAnim() {
		AnimationDesc fa = renderer.getCurrentAnimation();

		if (fa != null) {

			if (fa.sound != null) {
				playSound(fa.sound);
			}

			Vector2 inD = fa.inD;

			if (inD != null) {
				float s = EngineAssetManager.getInstance().getScale();

				setPosition(getX() + inD.x * s, getY() + inD.y * s);
			}
		}
	}

	/**
	 * Create position animation.
	 */
	public void startPosAnimation(Tween.Type repeatType, int count, float duration, float destX, float destY,
			InterpolationMode interpolation, ActionCallback cb) {

		posTween = new SpritePosTween();

		posTween.start(this, repeatType, count, destX, destY, duration, interpolation, cb);
	}

	/**
	 * Create scale animation.
	 */
	public void startScaleAnimation(Tween.Type repeatType, int count, float duration, float scale,
			InterpolationMode interpolation, ActionCallback cb) {

		scaleTween = new SpriteScaleTween();

		scaleTween.start(this, repeatType, count, scale, duration, interpolation, cb);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());

		sb.append("  Sprite Bbox: ").append(getBBox().toString());

		sb.append(renderer);

		return sb.toString();
	}

	@Override
	public void loadAssets() {
		super.loadAssets();

		renderer.loadAssets();
	}

	@Override
	public void retrieveAssets() {
		renderer.retrieveAssets();

		// Call setPosition to recalc fake depth and camera follow
		setPosition(bbox.getX(), bbox.getY());

		super.retrieveAssets();
	}

	@Override
	public void dispose() {
		renderer.dispose();
	}

	@Override
	public void write(Json json) {
		super.write(json);
		json.writeValue("renderer", renderer, null);

		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {

		} else {
			json.writeValue("posTween", posTween, null);
			json.writeValue("scaleTween", scaleTween, null);
		}
		
		json.writeValue("scale", scale);
		json.writeValue("depthType", depthType);
		json.writeValue("bboxFromRenderer", bboxFromRenderer);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);

		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
			renderer = json.readValue("renderer", ActorRenderer.class, jsonData);
		} else {
			posTween = json.readValue("posTween", SpritePosTween.class, jsonData);
			scaleTween = json.readValue("scaleTween", SpriteScaleTween.class, jsonData);
			renderer.read(json, jsonData.get("renderer"));
		}
		
		scale = json.readValue("scale", Float.class, jsonData);
		
		depthType = json.readValue("depthType", DepthType.class, jsonData);
		bboxFromRenderer = json.readValue("bboxFromRenderer", Boolean.class, jsonData);

		if (bboxFromRenderer)
			renderer.updateBboxFromRenderer(bbox);

		setScale(scale);
	}

}