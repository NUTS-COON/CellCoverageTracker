using System;
using System.Collections.Generic;

namespace Api.Models
{
    public class CellPoint : Coordinate
    {
        public CellPoint()
        {
        }
        
        public CellPoint(CellInfoMongoModel x)
        {
            OperatorName = x.OperatorName;
            CellType = x.CellType;
            Level = x.Level;
            Latitude = x.Location.Latitude;
            Longitude = x.Location.Longitude;
        }
        
        public string OperatorName { get; set; }
        public string CellType { get; set; }
        public int Level { get; set; }
    }

    public class CellPointComparer : IEqualityComparer<CellPoint>
    {
        public bool Equals(CellPoint x, CellPoint y) => 
            x.Latitude == y.Latitude && x.Longitude == y.Longitude && x.CellType == y.CellType;

        public int GetHashCode(CellPoint obj) => HashCode.Combine(obj.Latitude, obj.Longitude, obj.CellType);
    }
}
