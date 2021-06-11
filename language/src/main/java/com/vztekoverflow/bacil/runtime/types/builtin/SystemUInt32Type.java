package com.vztekoverflow.bacil.runtime.types.builtin;

import com.oracle.truffle.api.CompilerDirectives;
import com.vztekoverflow.bacil.BACILInternalError;
import com.vztekoverflow.bacil.parser.cli.CLIComponent;
import com.vztekoverflow.bacil.parser.cli.tables.generated.CLITypeDefTableRow;
import com.vztekoverflow.bacil.runtime.EvaluationStackPrimitiveMarker;
import com.vztekoverflow.bacil.runtime.locations.LocationsHolder;
import com.vztekoverflow.bacil.runtime.types.TypeHelpers;

/**
 * Implementation of the System.UInt32 type.
 */
public class SystemUInt32Type extends SystemValueTypeType {
    public SystemUInt32Type(CLITypeDefTableRow type, CLIComponent component) {
        super(type, component);
    }

    @Override
    public void locationToStack(LocationsHolder holder, int holderOffset, Object[] refs, long[] primitives, int slot) {
        refs[slot] = EvaluationStackPrimitiveMarker.EVALUATION_STACK_INT32;
        primitives[slot] = TypeHelpers.truncate32(holder.getPrimitives()[holderOffset]);
    }

    @Override
    public void stackToLocation(LocationsHolder holder, int holderOffset, Object ref, long primitive) {
        if(ref != EvaluationStackPrimitiveMarker.EVALUATION_STACK_INT32)
        {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new BACILInternalError("Saving a non-Int32 value into System.UInt32 location.");
        }
        holder.getPrimitives()[holderOffset]= TypeHelpers.truncate32(primitive);
    }

    @Override
    public void objectToStack(Object[] refs, long[] primitives, int slot, Object value) {
        refs[slot] = EvaluationStackPrimitiveMarker.EVALUATION_STACK_INT32;
        primitives[slot] = TypeHelpers.truncate32((Integer)value);
    }

    @Override
    public Object stackToObject(Object ref, long primitive) {
        if(ref != EvaluationStackPrimitiveMarker.EVALUATION_STACK_INT32)
        {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new BACILInternalError("Accessing a non-Int32 value from a System.UInt32 location.");
        }
        return (int) primitive;
    }

    @Override
    public void objectToLocation(LocationsHolder holder, int holderOffset, Object value) {
        holder.getPrimitives()[holderOffset] = TypeHelpers.truncate32((Integer) value);
    }

    @Override
    public Object locationToObject(LocationsHolder holder, int holderOffset) {
        return (int)holder.getPrimitives()[holderOffset];
    }
}
