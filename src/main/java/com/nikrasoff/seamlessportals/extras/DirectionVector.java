package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.Vector3;

public enum DirectionVector {
    POS_Z(new Vector3(0, 0, 1), "posZ"),
    NEG_Z(new Vector3(0, 0, -1), "negZ"),
    POS_X(new Vector3(1, 0, 0), "posX"),
    NEG_X(new Vector3(-1, 0, 0), "negX"),
    POS_Y(new Vector3(0, 1, 0), "posY"),
    NEG_Y(new Vector3(0, -1, 0), "negY");

    public static final DirectionVector[] allDirections = new DirectionVector[]{POS_Z, NEG_Z, NEG_X, POS_X, POS_Y, NEG_Y};
    public static final DirectionVector[] horizontalDirections = new DirectionVector[]{POS_Z, NEG_Z, NEG_X, POS_X};
    public static final DirectionVector[] verticalDirections = new DirectionVector[]{POS_Y, NEG_Y};

    private final Vector3 vector;
    private final String name;

    private DirectionVector(Vector3 from, String name){
        this.vector = from;
        this.name = name;
    }
    public Vector3 getVector(){
        return this.vector;
    }
    public String getName(){
        return name;
    }

    public static DirectionVector getClosestDirection(Vector3 from){
        return getClosestDirectionVector(from, allDirections);
    }
    public static DirectionVector getClosestHorizontalDirection(Vector3 from){
        return getClosestDirectionVector(from, horizontalDirections);
    }
    public static DirectionVector getClosestVerticalDirection(Vector3 from){
        return getClosestDirectionVector(from, verticalDirections);
    }

    public static DirectionVector getClosestDirectionVector(Vector3 from, DirectionVector[] checkDirections){
        DirectionVector result = POS_Z;
        float biggestDot = -100;
        for (DirectionVector v : checkDirections){
            float thisDot = v.getVector().dot(from);
            if (thisDot > biggestDot){
                result = v;
                biggestDot = thisDot;
            }
        }
        return result;
    }

    public static Vector3 getClosestVector(Vector3 from, Vector3[] checkVectors){
        Vector3 result = new Vector3();
        float biggestDot = -100;
        for (Vector3 v : checkVectors){
            float thisDot = v.dot(from);
            if (thisDot > biggestDot){
                result.set(v);
                biggestDot = thisDot;
            }
        }
        return result;
    }
}
