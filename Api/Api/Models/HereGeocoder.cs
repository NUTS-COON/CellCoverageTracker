﻿using System.Collections.Generic;
using System.Linq;

namespace Api.Models
{
    public class HereGeocoder
    {
        public HereGeocoderResponse Response { get; set; }

        public Coordinate GetCoordinate() => Response?.View?.FirstOrDefault()?.Result?.FirstOrDefault()?.Location?.DisplayPosition;
    }

    public class HereGeocoderResponse
    {
        public IEnumerable<HereGeocoderView> View { get; set; }
    }

    public class HereGeocoderView
    {
        public IEnumerable<HereGeocoderViewResult> Result { get; set; }
    }

    public class HereGeocoderViewResult
    {
        public HereGeocoderViewLocation Location { get; set; }
    }

    public class HereGeocoderViewLocation
    {
        public Coordinate DisplayPosition { get; set; }
    }
}
