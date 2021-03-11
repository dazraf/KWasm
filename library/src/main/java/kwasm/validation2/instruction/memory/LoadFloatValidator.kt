/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kwasm.validation2.instruction.memory

import kwasm.ast.instruction.MemoryInstruction
import kwasm.ast.type.ValueType
import kwasm.util.Impossible
import kwasm.validation.FunctionBodyValidationVisitor
import kwasm.validation.ValidationContext
import kwasm.validation.validate
import kwasm.validation.validateNotNull
import kwasm.validation2.InstructionSequenceState
import kwasm.validation2.InstructionValidator
import kwasm.validation2.ValidationValueType
import kwasm.validation2.toValidationValueType

/**
 * Validator of [MemoryInstruction.LoadFloat] nodes.
 *
 * From
 * [the docs](https://webassembly.github.io/spec/core/valid/instructions.html#memory-instructions):
 *
 * ```
 *   t.load memarg
 * ```
 * * The memory `C.mems[0]` must be defined in the context.
 * * The alignment `2^memarg.align` must not be larger than the bit width of `t` divided by `8`.
 * * Then the instruction is valid with type `\[i32] => \[t]`.
 */
object LoadFloatValidator : InstructionValidator<MemoryInstruction.LoadFloat> {
    override fun validate(
        node: MemoryInstruction.LoadFloat,
        context: ValidationContext.FunctionBody,
        state: InstructionSequenceState
    ) {
        context.validateMemoryExists()
        validate(node.arg.isAlignmentValid(node.byteWidth), parseContext = null) {
            "Invalid alignment for N=${node.byteWidth} (alignment must not be larger than natural)"
        }
        state.popOperand(ValidationValueType.I32)

        val resultType = when (node.bitWidth) {
            32 -> ValueType.F32
            64 -> ValueType.F64
            else -> Impossible()
        }
        state.pushOperand(resultType.toValidationValueType())
    }
}
