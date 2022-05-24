/*
 * Copyright (c) 2016-2022 Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.spiralhalo.plumbum.renderer.aocalc;

import io.github.spiralhalo.plumbum.Plumbum;

@FunctionalInterface
interface AoVertexClampFunction {
	float clamp(float x);

	AoVertexClampFunction CLAMP_FUNC = Plumbum.FIX_EXTERIOR_VERTEX_LIGHTING ? x -> x < 0f ? 0f : (x > 1f ? 1f : x) : x -> x;
}
