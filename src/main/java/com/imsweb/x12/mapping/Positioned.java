package com.imsweb.x12.mapping;

import java.util.Objects;

public interface Positioned extends Comparable<Positioned> {
  String getXid();
  String getPos();

  @Override
  default int compareTo(Positioned o) {
    Objects.requireNonNull(o);
    if (getPos().equals(o.getPos())) {
      return getXid().compareTo(o.getXid());
    }
    return getPos().compareTo(o.getPos());
  }
}
