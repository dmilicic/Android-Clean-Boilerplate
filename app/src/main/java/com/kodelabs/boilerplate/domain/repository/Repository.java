package com.kodelabs.boilerplate.domain.repository;

import com.kodelabs.boilerplate.domain.model.SampleModel;

/**
 * Created by dmilicic on 12/13/15.
 */
public interface Repository {

    boolean insert(SampleModel model);

    boolean update(SampleModel model);

    SampleModel get(Object id);

    boolean delete(SampleModel model);
}
