using System;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Driver.GeoJsonObjectModel;

namespace Api.Models
{
    public class CellInfoMongoModel
    {
        public CellInfoMongoModel(CellInfoSaveRequest x)
        {
            OperatorName = x.OperatorName;
            CellType = x.CellType;
            Level = x.Level;
            IMEI = x.IMEI;
            Timestamp = x.Timestamp;
            Location = new GeoJson2DGeographicCoordinates((double)x.Longitude, (double)x.Latitude);
        }
        
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }
        public string OperatorName { get; set; }
        public GeoJson2DGeographicCoordinates Location { get; set; }
        public string CellType { get; set; }
        public int Level { get; set; }
        public string IMEI { get; set; }
        public DateTime Timestamp { get; set; }
    }
}