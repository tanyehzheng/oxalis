/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.persistence.jdbc;

import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author steinar
 *         Date: 11.01.2017
 *         Time: 18.03
 */
public class SimpleStatisticsRepositoryImpl implements RawStatisticsRepository {

    public static final Logger log = LoggerFactory.getLogger(SimpleStatisticsRepositoryImpl.class);
    @Override
    public Integer persist(RawStatistics rawStatistics) {
        log.warn("Raw statistics not persisted - implementation is empty");
        return null;
    }

    @Override
    public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {

    }
}
